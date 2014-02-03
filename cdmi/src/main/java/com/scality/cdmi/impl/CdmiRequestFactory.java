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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;

import com.scality.cdmi.api.CdmiConfigurationException;
import com.scality.cdmi.api.RequestFactory;
import com.scality.cdmi.connector.PutRequestBuilder;

/**
 * @author ziad.bizri@ezako.com for Scality
 *
 */
public class CdmiRequestFactory extends RequestFactory {
    private String cdmiVersion;

    /**
     * @param uri
     * @param cdmiVersion
     */
    public CdmiRequestFactory(URI uri, String cdmiVersion) {
        super(uri.getScheme(), uri.getHost(), uri.getPort());
        this.cdmiVersion = cdmiVersion;
    }

    /**
     * @param uri
     * @param nonCdmiUri
     * @param cdmiVersion
     */
    public CdmiRequestFactory(URI uri, URI nonCdmiUri, String cdmiVersion) {
        super(uri.getScheme(), uri.getHost(), uri.getPort(), nonCdmiUri
                .getHost(), nonCdmiUri.getPort());
        this.cdmiVersion = cdmiVersion;
    }

    /**
     * @param scheme
     * @param host
     * @param port
     * @param cdmiVersion
     */
    public CdmiRequestFactory(String scheme, String host, int port,
            String cdmiVersion) {
        super(scheme, host, port);
        this.cdmiVersion = cdmiVersion;
    }

    @Override
    public PutRequestBuilder newPut(String containerType, String key)
            throws CdmiConfigurationException {
        return newPut(containerType, key, null);
    }

    @Override
    public PutRequestBuilder newPut(String containerType, String key,
            String query) throws CdmiConfigurationException {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        key = URIEscapeString(key);
        try {
            HttpPut put = new HttpPut(new URI(scheme, null, host, port, key,
                    query, null));
            put.addHeader("Accept", containerType);
            put.addHeader("Content-Type", containerType);
            put.addHeader("X-CDMI-Specification-Version", cdmiVersion);
            return new CdmiPutRequestBuilder(put);
        } catch (URISyntaxException e) {
            throw new CdmiConfigurationException(e);
        }
    }

    @Override
    public PutRequestBuilder newPutWithRange(String containerType, String key,
            long offset, long length) throws CdmiConfigurationException {
        return newPut(containerType, key,
                String.format("value:%d-%d", offset, offset + length - 1));
    }

    @Override
    public HttpGet newGet(String key) throws CdmiConfigurationException {
        return newGet(key, null);
    }

    @Override
    public HttpGet newGet(String key, String query)
            throws CdmiConfigurationException {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        key = URIEscapeString(key);
        try {
            HttpGet get = new HttpGet(new URI(scheme, null, host, port, key,
                    query, null));
            get.addHeader("X-CDMI-Specification-Version", cdmiVersion);
            return get;
        } catch (URISyntaxException e) {
            throw new CdmiConfigurationException(e);
        }
    }

    @Override
    public HttpGet newGetWithRange(String key, long offset, long length)
            throws CdmiConfigurationException {
        return newGet(key,
                String.format("value:%d-%d", offset, offset + length - 1));
    }

    @Override
    public HttpDelete newDelete(String key) throws CdmiConfigurationException {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        key = URIEscapeString(key);
        try {
            HttpDelete delete = new HttpDelete(new URI(scheme, null, host,
                    port, key, null, null));
            delete.addHeader("X-CDMI-Specification-Version", cdmiVersion);
            return delete;
        } catch (URISyntaxException e) {
            throw new CdmiConfigurationException(e);
        }
    }

    @Override
    public HttpHead newHead(String key) throws CdmiConfigurationException {
        if (!key.startsWith("/")) {
            key = "/" + key;
        }
        key = URIEscapeString(key);
        try {
            HttpHead head = new HttpHead(new URI(scheme, null, host, port, key,
                    null, null));
            return head;
        } catch (URISyntaxException e) {
            throw new CdmiConfigurationException(e);
        }
    }
}
