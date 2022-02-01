/*
 * Copyright (c) IBM Corp. 2018, 2021.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Maksim Zinal (IBM) - Initial implementation
 */
import groovy.sql.Sql

class G {
    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("MaskBatcher")

    public static String igcDbName = null;
    public static String quoteStyle = null;
}

G.LOG.info "dsmask MaskBatcher v1.0 2021-11-25"

G.LOG.info "Reading configuration settings..."

Properties conf = new Properties()
new File("MaskBatcher-config.xml").withInputStream { is -> conf.loadFromXML(is) }

String igcUrl = conf.getProperty("igc.url")
String igcUser = conf.getProperty("igc.username")
String igcPass = conf.getProperty("igc.password")
String jobRunner = conf.getProperty("job.runner")
String jobLister = conf.getProperty("job.lister")

String jobFileName = (args.length > 0) ? args[0] : "MaskBatcher-jobfile.xml"

G.LOG.info "Reading job file {}...", jobFileName

Properties job = new Properties()
new File(jobFileName).withInputStream { is -> job.loadFromXML(is) }

G.igcDbName = job.getProperty("igc.dbname")
G.quoteStyle = job.getProperty("quote.style")
String inputConnection = job.getProperty("input.conn")
String outputConnection = job.getProperty("output.conn")

G.LOG.info "Preparing the list of tables for database {}...", G.igcDbName

def maskedTables = []

String igcQuery = '''
SELECT DISTINCT scm.name, tab.name
FROM CMVIEWS.DQCLASSIFICATION q
INNER JOIN CMVIEWS.DQDATACLASS dc
  ON q.OFDATACLASSRID=dc.rid
INNER JOIN CMVIEWS.PDRDATABASECOLUMN fld
  ON q.CLASSIFIESOBJECTRID=fld.rid
INNER JOIN CMVIEWS.PDRDATABASETABLE tab
  ON tab.rid=fld.OFDATABASETABLERID
INNER JOIN CMVIEWS.PDRDATABASESCHEMA scm
  ON scm.rid=tab.OFDATASCHEMARID
INNER JOIN CMVIEWS.PDRDATABASE db
  ON db.rid=scm.OFDATABASERID
INNER JOIN IGVIEWS.IGASSIGNEDOBJECTSOFATERM bto
  ON bto.CLASSIFIEDOBJECTRID=dc.RID
INNER JOIN IGVIEWS.IGBUSINESSTERM bt
  ON bt.RID=bto.BUSINESSTERMRID AND bt.ABBREVIATION='DsMask.C'
INNER JOIN IGVIEWS.IGBUSINESSCATEGORY bc
  ON bc.RID=bt.OFCATEGORY 
 AND bc.SHORTDESCRIPTION='DsMask.Mode'
WHERE COALESCE(q.State, '-') IN ('Approved', '-')
  AND LOWER(db.name)=LOWER(?)
ORDER BY scm.name, tab.name
'''

def makeProfile(String schemaName, String tableName) {
    return G.igcDbName.toLowerCase() + "." + schemaName.toLowerCase() + "." + tableName.toLowerCase()
}

def makeTableName(String sn, String tn) {
    if (G.quoteStyle==null || "none".equalsIgnoreCase(G.quoteStyle))
        return sn + "." + tn
    if ("mssql".equalsIgnoreCase(G.quoteStyle))
        return "[" + sn + "].[" + tn + "]"
    return '"' + sn + '"."' + tn + '"'
}

Sql.withInstance(igcUrl, igcUser, igcPass) { sql ->
    sql.eachRow(igcQuery, [G.igcDbName]) { row ->
        String sn = row.getString(1)
        String tn = row.getString(2)
        maskedTables.add([ makeProfile(sn, tn), makeTableName(sn, tn) ])
    }
}

if (maskedTables.isEmpty()) {
    G.LOG.info "Nothing needs to be masked."
    System.exit(0)
}

G.LOG.info "Input connection: {}, output connection: {}", inputConnection, outputConnection
maskedTables.each { tab ->
    G.LOG.info " ** will mask table {} -> {}", tab[1], tab[0]
}
G.LOG.info "Total tables to be masked: {}", maskedTables.size()

G.LOG.info "Checking for already running masking jobs on input connection..."

String cmd = jobLister + ' "' + inputConnection + '"'
do {
    def out = new StringBuffer();
    def err = new StringBuffer();
    Process proc = cmd.execute()
    proc.waitForProcessOutput(out, err)
    int statusCode = proc.exitValue()
    if (statusCode != 0) {
        G.LOG.error "Failed to retrieve list of jobs, status code {}", statusCode
        if (out.size() > 0) {
            G.LOG.warn "OUT> {}", out
        }
        if (err.size() > 0) {
            G.LOG.warn "ERR> {}", err
        }
        System.exit(1)
    }
    if ( out.size() > 0 ) {
        G.LOG.error "Found running jobs, refusing to start new ones.\n{}", out
        System.exit(1)
    }
} while (false)

String batchId = UUID.randomUUID().toString()

G.LOG.info "Starting new masking jobs with batch ID {}...", batchId

String cmdBase = jobRunner + ' "' + batchId + '"'
cmdBase = cmdBase + ' "' + inputConnection + '" "' + outputConnection + '"'
maskedTables.each { tab ->
    cmd = cmdBase + ' "' + tab[1] + '" "' + tab[0] + '"'
    G.LOG.info " ** {}", cmd
    def out = new StringBuffer();
    def err = new StringBuffer();
    Process proc = cmd.execute()
    proc.waitForProcessOutput(out, err)
    int statusCode = proc.exitValue()
    if (statusCode != 0) {
        G.LOG.error "Job startup returned non-zero status code {}", statusCode
        if (out.size() > 0) {
            G.LOG.warn "OUT> {}", out
        }
        if (err.size() > 0) {
            G.LOG.warn "ERR> {}", err
        }
    }
}

G.LOG.info "Masking jobs have been started, please stand by..."
