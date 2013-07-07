/*
 * Copyright (C) 2013 SCALITY SA. All rights reserved.
 * http://www.scality.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SCALITY SA ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCALITY SA OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of SCALITY SA.
 *
 * https://github.com/scality/CaDMIum
 */
package com.scality.cdmi.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import com.scality.cdmi.api.CdmiAuthScope;
import com.scality.cdmi.api.CdmiClient;
import com.scality.cdmi.api.CdmiConnectionManager;
import com.scality.cdmi.api.CdmiCredentials;
import com.scality.cdmi.api.RequestFactory;
import com.scality.cdmi.api.RetryStrategy;

/**
 * Maintains a pool of threads used for sending requests in parallel.
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class CdmiPooledConnectionManager extends CdmiConnectionManager {
    private SchemeRegistry schemeRegistry;
    private PoolingClientConnectionManager connectionManager;
    private DefaultHttpClient httpClient;
    private CdmiClient client;

    /**
     * Constructor
     * @param factory
     * @param authscope
     * @param credentials
     * @param retryStrategy
     * @param max_total_connection
     * @param ioBufferSize
     * @param maxPutSize
     */
    public CdmiPooledConnectionManager(RequestFactory factory, CdmiAuthScope authscope,
            CdmiCredentials credentials, RetryStrategy retryStrategy, int max_total_connection,
            int ioBufferSize, int maxPutSize) {
        schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(max_total_connection);
        connectionManager.setDefaultMaxPerRoute(max_total_connection);

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                retryStrategy.getTimeOutMillis());
        params.setParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 524288);
        httpClient = new DefaultHttpClient(connectionManager, params);
        httpClient.getCredentialsProvider().setCredentials(authscope.getScope(),
                credentials.getCred());
        client = new CdmiClientImpl(httpClient, factory, retryStrategy, ioBufferSize,
                maxPutSize, true /*multiThreaded*/);
    }

    @Override
    public CdmiClient getClient() {
        return client;
    }
    
    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public void shutdown() {
        if (connectionManager != null) {
            connectionManager.shutdown();
            connectionManager = null;
        }
        if (schemeRegistry != null) {
            for (String name : schemeRegistry.getSchemeNames()) {
                schemeRegistry.unregister(name);
            }
            schemeRegistry = null;
        }
    }
}
