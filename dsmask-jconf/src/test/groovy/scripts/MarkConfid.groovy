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

class G {
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("dsmask-MarkConfid")
    // Global settings
    static String IIS_URL = null
    static String IIS_SEARCH_URL = null
    // Cache of term IDs [type, id]
    static final Map<String, String> TERM_IDS = new HashMap<>()
}

// Lookup term by its abbreviation
def findTermId(CloseableHttpClient iis, String termAbbrev) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"name\",\"abbreviation\"]")
    input.append(",\"types\":[\"term\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"abbreviation\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(termAbbrev.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def confidId = jsonItems[0]["_id"].toString()

    G.LOG.info "Label term of type {} found: {}", termAbbrev, confidId
    return confidId
}

def safeFindTermId(CloseableHttpClient iis, String termAbbrev) {
    def retval = G.TERM_IDS.get(termAbbrev)
    if (retval != null)
        return retval
    retval = findTermId(iis, termAbbrev)
    if (retval==null)
        throw new Exception("Failed to find term by abbreviation '" + termAbbrev + "'")
    G.TERM_IDS.put(termAbbrev, retval)
    return retval
}

// Look up and process a single dataclass definition
def markDataClass(CloseableHttpClient iis, String refname, String termId) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"class_code\",\"assigned_to_terms\"]")
    input.append(",\"types\":[\"data_class\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"class_code\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(refname.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
    if (output==null || output.length()==0) {
        G.LOG.warn "Failed to find dataclass {}, SKIPPED", refname
        return null
    }
    def jsonData = new JsonSlurper().parseText(output)["items"][0]
    if (jsonData==null) {
        G.LOG.warn "Failed to process dataclass {}, SKIPPED", refname
        return null
    }
    def refid = jsonData["_id"].toString()
    G.LOG.info "Dataclass {} found: {}", refname, refid

    def hasTerm = false
    def havingTerms = jsonData["assigned_to_terms"]["items"]
    if (havingTerms != null && havingTerms.size() > 0) {
        havingTerms.each { it ->
            if ( termId.equalsIgnoreCase(it["_id"]) ) {
                hasTerm = true
            }
        }
    }

    if (hasTerm) {
        G.LOG.info "Dataclass {} already marked, SKIPPED", refname
        return refid
    }

    input = new StringBuilder()
    input.append("{\"items\": [{\"_id\": \"").
        append(termId).append("\"}]}")
    def updateUrl = G.IIS_URL + "/ibm/iis/igc-rest/v1/assets/" +
        refid + "/assigned_to_terms"
    output = HttpHelper.putJsonText(iis, updateUrl, input.toString())

    G.LOG.info "Dataclass {} successfully marked", refname
    return refid
}

// Look up and process a single dataclass definition
def markTerm(CloseableHttpClient iis, String refname, String termId) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"abbreviation\",\"assigned_terms\"]")
    input.append(",\"types\":[\"term\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"abbreviation\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(refname.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
    if (output==null || output.length()==0) {
        G.LOG.warn "Failed to find term {}, SKIPPED", refname
        return null
    }
    def jsonData = new JsonSlurper().parseText(output)["items"][0]
    if (jsonData==null) {
        G.LOG.warn "Failed to process term {}, SKIPPED", refname
        return null
    }
    def refid = jsonData["_id"].toString()
    G.LOG.info "Term {} found: {}", refname, refid

    def hasTerm = false
    def havingTerms = jsonData["assigned_terms"]["items"]
    if (havingTerms != null && havingTerms.size() > 0) {
        havingTerms.each { it ->
            if ( termId.equalsIgnoreCase(it["_id"]) ) {
                hasTerm = true
            }
        }
    }

    if (hasTerm) {
        G.LOG.info "Term {} already marked, SKIPPED", refname
        return refid
    }

    input = new StringBuilder()
    input.append("{\"items\": [{\"_id\": \"").
        append(termId).append("\"}]}")
    def updateUrl = G.IIS_URL + "/ibm/iis/igc-rest/v1/assets/" +
        refid + "/assigned_terms"
    output = HttpHelper.putJsonText(iis, updateUrl, input.toString())

    G.LOG.info "Term {} successfully marked", refname
    return refid
}

// Grab the list of dataclass names
def grabFromConfig(Properties conf, String confname) {
    def ret = []
    for (int i=0; i<1000; ++i) {
        String propname = confname + "." + String.valueOf(i)
        String propval = conf.getProperty(propname)
        if (propval != null)
            ret.add(propval)
    }
    return ret
}

// Operations sequence using the IIS services connection
def runAll(CloseableHttpClient iis, Properties job) {
    grabFromConfig(job, "term.G").each { it ->
        markTerm(iis, it, safeFindTermId(iis, "DsMask.G"))
    }
    grabFromConfig(job, "term.C").each { it ->
        markTerm(iis, it, safeFindTermId(iis, "DsMask.C"))
    }
    grabFromConfig(job, "term.R").each { it ->
        markTerm(iis, it, safeFindTermId(iis, "DsMask.R"))
    }
    grabFromConfig(job, "dcs.G").each { it ->
        markDataClass(iis, it, safeFindTermId(iis, "DsMask.G"))
    }
    grabFromConfig(job, "dcs.C").each { it ->
        markDataClass(iis, it, safeFindTermId(iis, "DsMask.C"))
    }
    grabFromConfig(job, "dcs.R").each { it ->
        markDataClass(iis, it, safeFindTermId(iis, "DsMask.R"))
    }
}

G.LOG.info "dsmask MarkConfid v1.1 2022-03-31"

final Properties conf = new Properties(), job = new Properties()
new File("MarkConfid-config.xml").withInputStream { is -> conf.loadFromXML(is) }
if (this.args.length == 0) {
    new File("MarkConfid-job.xml").withInputStream { is -> conf.loadFromXML(is) }
} else {
    new File(this.args[0]).withInputStream { is -> conf.loadFromXML(is) }
}

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
hc.withCloseable { iis -> runAll(iis, job) }

G.LOG.info "dsmask MarkConfid shutting down..."

// End Of File
