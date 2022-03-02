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
package com.ibm.dsmask.hc;

import net.dsmask.util.PasswordVault;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.http.ssl.TLS;

/**
 * Apache HTTP client wrapper for automatic configuration.
 * @author zinal
 */
public class HttpHelper {

    public static CloseableHttpClient newClient(String baseUrl, String vaultId)
            throws Exception {
        PasswordVault.Entry e = new PasswordVault().getEntry(vaultId);
        if (e==null) {
            throw new RuntimeException("Illegal password vault ID: " + vaultId);
        }
        return newClient(baseUrl, e.login, e.password);
    }

    public static CloseableHttpClient newClient(String baseUrl, String login,
            String password) throws Exception {
        return newClient(baseUrl, login,
                (password==null) ? (char[])null : password.toCharArray());
    }

    public static CloseableHttpClient newClient(String baseUrl, String login,
            char[] password) throws Exception {
        final URI uri = new URI(baseUrl);
        // Trust-all context.
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(
                (X509Certificate[] chain, String authType) -> true) . build();
        // Set TLS protocol versions and disable host name verification.
        SSLConnectionSocketFactory sslSocketFactory =
            SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslcontext)
                    .setHostnameVerifier(new NoopHostnameVerifier())
                    .setTlsVersions(TLS.V_1_2/*, TLS.V_1_3*/)
                    .build();
        HttpClientConnectionManager connManager =
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        // login + password
        BasicCredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(
            new AuthScope(new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort())),
            new UsernamePasswordCredentials(login, password)
        );
        CloseableHttpClient hc = HttpClients.custom()
                .setDefaultCredentialsProvider(credProvider)
                .setConnectionManager(connManager)
                .build();
        try {
            HttpGet request = new HttpGet(baseUrl);
            try ( CloseableHttpResponse response = hc.execute(request) ) {
                if (response.getCode() != 200) {
                    throw new Exception("Request to '" + baseUrl + "' "
                            + "failed with code "
                            + response.getCode() + ": "
                            + response.getReasonPhrase());
                }
            }
            CloseableHttpClient retval = hc;
            hc = null;
            return retval;
        } finally {
            if (hc!=null)
                hc.close();
        }
    }

    public static byte[] postJsonBin(CloseableHttpClient hc, String url, byte[] input)
            throws Exception {
        HttpPost request = new HttpPost(url);
        if (input!=null) {
            request.setEntity(new InputStreamEntity(
                    new ByteArrayInputStream(input), input.length,
                    ContentType.APPLICATION_JSON));
        }
        try ( CloseableHttpResponse response = hc.execute(request) ) {
            if (response.getCode() != 200) {
                throw toException(url, response);
            }
            try ( InputStream stream = response.getEntity().getContent() ) {
                return IOUtils.toByteArray(stream);
            }
        }
    }

    public static String postJsonText(CloseableHttpClient hc, String url, String input)
            throws Exception {
        byte[] inputBytes =
                (input==null) ? null : input.getBytes(StandardCharsets.UTF_8);
        byte[] outputBytes = postJsonBin(hc, url, inputBytes);
        if (outputBytes==null)
            return null;
        if (outputBytes.length==0)
            return "";
        return new String(outputBytes, StandardCharsets.UTF_8);
    }

    public static byte[] putJsonBin(CloseableHttpClient hc, String url, byte[] input)
            throws Exception {
        HttpPut request = new HttpPut(url);
        if (input!=null) {
            request.setEntity(new InputStreamEntity(
                    new ByteArrayInputStream(input), input.length,
                    ContentType.APPLICATION_JSON));
        }
        try ( CloseableHttpResponse response = hc.execute(request) ) {
            if (response.getCode() != 200) {
                throw toException(url, response);
            }
            try ( InputStream stream = response.getEntity().getContent() ) {
                return IOUtils.toByteArray(stream);
            }
        }
    }

    public static String putJsonText(CloseableHttpClient hc, String url, String input)
            throws Exception {
        byte[] inputBytes =
                (input==null) ? null : input.getBytes(StandardCharsets.UTF_8);
        byte[] outputBytes = putJsonBin(hc, url, inputBytes);
        if (outputBytes==null)
            return null;
        if (outputBytes.length==0)
            return "";
        return new String(outputBytes, StandardCharsets.UTF_8);
    }

    public static Exception toException(String url, CloseableHttpResponse response) {
        return new Exception("Request to '" + url + "' "
                        + "failed with code "
                        + response.getCode() + ": "
                        + response.getReasonPhrase());
    }
}
