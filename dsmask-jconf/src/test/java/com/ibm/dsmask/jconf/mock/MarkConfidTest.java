package com.ibm.dsmask.jconf.mock;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import com.ibm.dsmask.hc.HttpHelper;
// import org.junit.Test;

/**
 *
 * @author zinal
 */
public class MarkConfidTest {
    
//    @Test
    public void runTest() throws Exception {
        final String iisUrl = "https://localhost:9443";
        final String iisUser = "isadmin";
        final String iisPassword = "P@ssw0rd";
        
        String fullUrl = iisUrl + "/ibm/iis/igc-rest/v1/search";
        
        final StringBuilder input = new StringBuilder();
        input.append("{\"pageSize\":1,\"properties\":[\"name\",\"class_code\"]");
        input.append(",\"types\":[\"data_class\"]");
        input.append(",\"where\": {\"conditions\":[");
        input.append("{\"property\": \"class_code\",\"operator\": \"=\"");
        input.append(",\"value\": \"").append("DOCDATE_RUS").append("\"}]");
        input.append(",\"operator\": \"and\"}}");
        
        try (CloseableHttpClient chc = HttpHelper.newClient(fullUrl, iisUser, iisPassword)) {
            String output = HttpHelper.postJsonText(chc, fullUrl, input.toString());
            System.out.println(output);
        }
    }
    
}
