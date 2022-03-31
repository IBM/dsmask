/*
 * Copyright (c) IBM Corp. 2018, 2022.
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
import groovy.json.JsonSlurper
import com.ibm.dsmask.hc.HttpHelper
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.commons.text.StringTokenizer

class G {
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("dsmask-MarkFields")
    // Global settings
    static String IIS_URL = null
    static String IIS_SEARCH_URL = null
    // Cache of dataclass/term IDs [type, X:id], where X={D,T}
    static final Map<String, String> KNOWN_IDS = new HashMap<>()
    // Cache of incorrect IDs
    static final Set<String> BAD_IDS = new HashSet<>()
}

class Input {
    final Map<String, Map<String, Map<String, Map<String, String>>>> data = new TreeMap<>();
}

// Lookup term by its abbreviation
def directFindTermId(CloseableHttpClient iis, String code) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"name\",\"abbreviation\"]")
    input.append(",\"types\":[\"term\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"abbreviation\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(code.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def retval = jsonItems[0]["_id"].toString()

    G.LOG.info "Label term of type {} found: {}", code, retval
    return retval
}

// Lookup dataclass by its code
def directFindDcsId(CloseableHttpClient iis, String code) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"name\",\"class_code\"]")
    input.append(",\"types\":[\"data_class\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"class_code\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(code.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def retval = jsonItems[0]["_id"].toString()

    G.LOG.info "Label dataclass of type {} found: {}", code, retval
    return retval
}

def safeFindId(CloseableHttpClient iis, String code) {
    def retval = G.KNOWN_IDS.get(code)
    if (retval != null)
        return retval
    retval = directFindDcsId(iis, code)
    if (retval != null) {
        retval = "D:" + retval
    } else {
        retval = directFindTermId(iis, code)
        if (retval != null) {
            retval = "T:" + retval
        }
    }
    if (retval==null)
        throw new Exception("Failed to find dataclass or term by code '" + code + "'")
    G.KNOWN_IDS.put(code, retval)
    return retval
}

def mapOne(CloseableHttpClient iis, String db, String schema, String table, String field, String dcs) {
    G.LOG.info "Mapping {}.{}.{} {} -> {}", db, schema, table, field, dcs
}

// Operations sequence using the IIS services connection
def runAll(CloseableHttpClient iis, Input input) {
    input.data.each { db ->
        db.value.each { schema ->
            schema.value.each { table ->
                table.value.each { field ->
                    mapOne(iis, db.key, schema.key, table.key, field.key, field.value)
                }
            }
        }
    }
}

def loadInputFile(InputStream is, Input input) {
    is.eachLine { line ->
        final String[] items = new StringTokenizer(line).getTokenArray()
        if (items.length != 5 && line.trim().length() > 0) {
            G.LOG.warn "Bad line format: {}", line
        } else {
            final String db = items[0]
            final String schema = items[1]
            final String table = items[2]
            final String field = items[3]
            final String dcs = items[4]
            def schemas = input.data.get(db)
            if (schemas==null) {
                schemas = new TreeMap<>()
                input.data.put(db, schemas)
            }
            def tables = schemas.get(schema)
            if (tables==null) {
                tables = new TreeMap<>()
                schemas.put(schema, tables)
            }
            def fields = tables.get(table)
            if (fields==null) {
                fields = new TreeMap<>()
                tables.put(table, fields)
            }
            if ( fields.put(field, dcs) != null ) {
                G.LOG.warn "Duplicate line: {}", line
            }
        }
    }
}

G.LOG.info "dsmask MarkFields v1.0 2022-03-31"

final Properties conf = new Properties()
new File("MarkConfid-config.xml").withInputStream { is -> conf.loadFromXML(is) }

// db -> {schema -> {table -> {field -> dcs}}}
final String inputFileName;
if (this.args.length == 0) {
    inputFileName = "MarkFields-input.csv"
} else {
    inputFileName = this.args[0]
}
final Input input = new Input();
new File(inputFileName).withInputStream { is -> loadInputFile(is, input) }

// The re-usable connection
G.IIS_URL = conf.getProperty("iis.url", "https://localhost:9443")
G.IIS_SEARCH_URL = G.IIS_URL + "/ibm/iis/igc-rest/v1/search"
final CloseableHttpClient hc;
if ( conf.getProperty("iis.vault") != null ) {
    hc = HttpHelper.newClient( G.IIS_SEARCH_URL,
            conf.getProperty("iis.vault", "isadmin") )
} else {
    hc = HttpHelper.newClient( G.IIS_SEARCH_URL,
            conf.getProperty("iis.username", "isadmin"),
            conf.getProperty("iis.password", "P@ssw0rd") )
}
// Main algorithm is executed on top of HTTP connection
hc.withCloseable { iis -> runAll(iis, input) }

G.LOG.info "dsmask MarkFields shutting down..."

// End Of File
