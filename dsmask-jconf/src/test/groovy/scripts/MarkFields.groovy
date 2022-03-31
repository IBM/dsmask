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
}

class FieldInfo {
    String name
    String id
    String dcs
}

class TableInfo {
    String name
    String id
    final Map<String, FieldInfo> items = new TreeMap<>()
}

class SchemaInfo {
    String name
    String id
    final Map<String, TableInfo> items = new TreeMap<>()
}

class DbInfo {
    String name
    String id
    final Map<String, SchemaInfo> items = new TreeMap<>()
}

class Input {
    final Map<String, DbInfo> data = new TreeMap<>();
}

String postJsonText(CloseableHttpClient iis, Object input) {
    return HttpHelper.postJsonText(iis, G.IIS_SEARCH_URL, input.toString())
}

// Lookup term by its abbreviation
String directFindTermId(CloseableHttpClient iis, String code) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"name\",\"abbreviation\"]")
    input.append(",\"types\":[\"term\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"abbreviation\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(code.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = postJsonText(iis, input)
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
String directFindDcsId(CloseableHttpClient iis, String code) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1,\"properties\":[\"name\",\"class_code\"]")
    input.append(",\"types\":[\"data_class\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"class_code\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(code.toUpperCase()).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = postJsonText(iis, input)
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

String safeFindId(CloseableHttpClient iis, String code) {
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

String directFindDb(CloseableHttpClient iis, String name) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1")
    input.append(",\"types\":[\"database\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"name\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(name).append("\"}]")
    input.append(",\"operator\": \"and\"}}")

    def output = postJsonText(iis, input)
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def retval = jsonItems[0]["_id"].toString()

    G.LOG.info "Database {} found: {}", name, retval
    return retval
}

String findDb(CloseableHttpClient iis, DbInfo info) {
    if (info.id==null) {
        info.id = directFindDb(iis, info.name)
    }
    if (info.id==null) {
        throw new Exception("Cannot find database '" + info.name + "'")
    }
    return info.id
}

String directFindSchema(CloseableHttpClient iis, String owner, String name) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1")
    input.append(",\"types\":[\"database_schema\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"name\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(name).append("\"}")
    input.append(",{\"property\": \"database\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(owner).append("\"}")
    input.append("],\"operator\": \"and\"}}")

    def output = postJsonText(iis, input)
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def retval = jsonItems[0]["_id"].toString()

    G.LOG.info "Schema {} found: {}", name, retval
    return retval
}

String findSchema(CloseableHttpClient iis, DbInfo db, SchemaInfo info) {
    if (info.id==null) {
        info.id = directFindSchema(iis, db.id, info.name)
    }
    if (info.id==null) {
        throw new Exception("Cannot find schema '" + info.name + "'")
    }
    return info.id
}

String directFindTable(CloseableHttpClient iis, String owner, String name) {
    def input = new StringBuilder()
    input.append("{\"pageSize\":1")
    input.append(",\"types\":[\"database_table\"]")
    input.append(",\"where\": {\"conditions\":[")
    input.append("{\"property\": \"name\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(name).append("\"}")
    input.append(",{\"property\": \"database_schema\",\"operator\": \"=\"")
    input.append(",\"value\": \"").append(owner).append("\"}")
    input.append("],\"operator\": \"and\"}}")

    def output = postJsonText(iis, input)
    if (output==null || output.length()==0)
        return null
    def jsonData = new JsonSlurper().parseText(output)
    def jsonItems = jsonData["items"]
    if (jsonItems.size()==0)
        return null
    def retval = jsonItems[0]["_id"].toString()

    G.LOG.info "Table {} found: {}", name, retval
    return retval
}

String findTable(CloseableHttpClient iis, SchemaInfo schema, TableInfo info) {
    if (info.id==null) {
        info.id = directFindTable(iis, schema.id, info.name)
    }
    if (info.id==null) {
        throw new Exception("Cannot find table '" + info.name + "'")
    }
    return info.id
}

def mapOne(CloseableHttpClient iis, DbInfo db, SchemaInfo schema,
        TableInfo table, FieldInfo field) {
    G.LOG.info "Mapping {}.{}.{} {} -> {}", db.name, schema.name,
        table.name, field.name, field.dcs
    findDb iis, db
    findSchema iis, db, schema
    findTable iis, schema, table
}

// Operations sequence using the IIS services connection
def runAll(CloseableHttpClient iis, Input input) {
    input.data.each { db ->
        db.value.items.each { schema ->
            schema.value.items.each { table ->
                table.value.items.each { field ->
                    mapOne(iis, db.value, schema.value, table.value, field.value)
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
            def dbInfo = input.data.get(db)
            if (dbInfo==null) {
                dbInfo = new DbInfo()
                dbInfo.name = db;
                input.data.put(db, dbInfo)
            }
            def schemaInfo = dbInfo.items.get(schema)
            if (schemaInfo==null) {
                schemaInfo = new SchemaInfo()
                schemaInfo.name = schema
                dbInfo.items.put(schema, schemaInfo)
            }
            def tableInfo = schemaInfo.items.get(table)
            if (tableInfo==null) {
                tableInfo = new TableInfo()
                tableInfo.name = table
                schemaInfo.items.put(table, tableInfo)
            }
            def fieldInfo = tableInfo.items.get(field)
            if (fieldInfo!=null) {
                G.LOG.warn "Duplicate line: {}", line
            } else {
                fieldInfo = new FieldInfo()
                fieldInfo.name = field
                fieldInfo.dcs = dcs
                tableInfo.items.put(field, fieldInfo)
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
