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
package com.scality.cdmi.api;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;

import com.scality.cdmi.connector.PutRequestBuilder;
import com.scality.cdmi.impl.CdmiRequestFactory;
import com.scality.cdmi.impl.NonCdmiRequestFactory;

/**
 * Class used for generating all the outgoing requests to the CDMI server.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public abstract class RequestFactory {

    /**
     * The scheme used, e.g. "http".
     */
    protected final String scheme;

    /**
     * The hostname or IP address of the CDMI server.
     */
    protected final String host;

    /**
     * The hostname or IP address of the CDMI server to use for non-CDMI
     * operations (such as file operations). Defaults to same as host;
     */
    protected final String nonCdmiHost;

    /**
     * The port to be used, e.g. 80.
     */
    protected final int port;

    /**
     * The port to be used for non-CDMI operations, e.g. 80. Defaults to same as
     * port.
     */
    protected final int nonCdmiPort;

    /**
     * Constructor.
     * 
     * @param scheme
     *            the scheme used.
     * @param host
     *            the hostname or IP address of the CDMI server.
     * @param port
     *            the port number, defaults to 80 if negative.
     */
    protected RequestFactory(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.nonCdmiHost = host;
        this.port = (port > 0) ? port : 80;
        this.nonCdmiPort = port;
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            the scheme used.
     * @param host
     *            the hostname or IP address of the CDMI server.
     * @param port
     *            the port number, defaults to 80 if negative.
     * @param nonCdmiHost
     *            the hostname or IP address of the CDMI server to be used for
     *            non-CDMI operations.
     * @param nonCdmiPort
     *            the port number, defaults to 80 if negative, of the CDMI
     *            server to be used for non-CDMI operations.
     */
    protected RequestFactory(String scheme, String host, int port,
            String nonCdmiHost, int nonCdmiPort) {
        this.scheme = scheme;
        this.host = host;
        this.nonCdmiHost = nonCdmiHost;
        this.port = (port > 0) ? port : 80;
        this.nonCdmiPort = nonCdmiPort;
    }

    /**
     * Some characters can cause confusion when used in container or data object
     * path. This method is a workaround and should be used any time a path
     * string is to be included in a request.
     * 
     * @param key
     *            the key to escape.
     * @return the key with some characters escaped.
     */
    public static String URIEscapeString(String key) {
        return key.replace("%", "%25").replace("/.", "/<dot>");
    }

    /**
     * Get a {@link PutRequestBuilder} for creating a PUT request for the
     * underlying resource.
     * 
     * @param resourceType
     *            the type of resource (data object or container).
     * @param key
     *            the name of the resource in the CDMI repository.
     * @return a {@link PutRequestBuilder} used for creating a PUT request.
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the builder.
     */
    public abstract PutRequestBuilder newPut(String resourceType, String key)
            throws CdmiConfigurationException;

    /**
     * Get a {@link PutRequestBuilder} for creating a PUT request for the
     * underlying resource, with the additional query fragment.
     * 
     * @param resourceType
     *            the type of resource (data object or container).
     * @param key
     *            the name of the resource in the CDMI repository.
     * @param query
     *            the query fragment to be included in the request.
     * @return a {@link PutRequestBuilder} used for creating a PUT request.
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the builder.
     */
    public abstract PutRequestBuilder newPut(String resourceType, String key,
            String query) throws CdmiConfigurationException;

    /**
     * Get a {@link PutRequestBuilder} for creating a PUT request for the
     * underlying resource for the specified range.
     * 
     * @param resourceType
     *            the type of resource (data object or container).
     * @param key
     *            the name of the resource in the CDMI repository.
     * @param offset
     *            the start position in the resource
     * @param length
     *            the length used in determining the range
     * @return a {@link PutRequestBuilder} used for creating a PUT request.
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the builder.
     */
    public abstract PutRequestBuilder newPutWithRange(String resourceType,
            String key, long offset, long length)
            throws CdmiConfigurationException;

    /**
     * Get a {@link HttpGet} request for querying a resource in the CDMI
     * repository.
     * 
     * @param key
     *            the name of the resource in the CDMI repository.
     * @return a {@link HttpGet} request
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the request.
     */
    public abstract HttpGet newGet(String key)
            throws CdmiConfigurationException;

    /**
     * Get a {@link HttpGet} request for querying a resource in the CDMI
     * repository.
     * 
     * @param key
     *            the name of the resource in the CDMI repository.
     * @param query
     *            the query to be sent out (e.g. "children") without the
     *            starting '?'
     * @return a {@link HttpGet} request
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the request.
     */
    public abstract HttpGet newGet(String key, String query)
            throws CdmiConfigurationException;

    /**
     * Get a {@link HttpGet} request for querying a resource in the CDMI
     * repository with a specific range.
     * 
     * @param key
     *            the name of the resource in the CDMI repository.
     * @param offset
     *            the starting offset for the request
     * @param length
     *            the length used in determining the range
     * @return a {@link HttpGet} request
     * @throws CdmiConfigurationException
     *             if there was any issue in creating the request.
     */
    public abstract HttpGet newGetWithRange(String key, long offset, long length)
            throws CdmiConfigurationException;

    /**
     * Get a {@link HttpDelete} request for deleting a resource in the CDMI
     * repository.
     * 
     * @param key
     *            the name of the resource in the CDMI repository
     * @return a {@link HttpDelete} request
     * @throws CdmiConfigurationException
     *             if there was any issue during the creation of the request.
     */
    public abstract HttpDelete newDelete(String key)
            throws CdmiConfigurationException;

    /**
     * Get a {@link HttpHead} request for sending a request to the CDMI
     * repository.
     * 
     * @param key
     *            the name of the resource in the CDMI repository
     * @return a {@link HttpHead} request
     * @throws CdmiConfigurationException
     *             if there was any issue during the creation of the request.
     */
    public abstract HttpHead newHead(String key)
            throws CdmiConfigurationException;

    /**
     * Create a factory for CDMI operations.
     * 
     * @param uri
     *            the {@link URI} of the CDMI server.
     * @param cdmiVersion
     *            the version to use in all requests.
     * @return a {@link RequestFactory} object.
     */
    public static RequestFactory newCdmiFactory(URI uri, String cdmiVersion) {
        return new CdmiRequestFactory(uri, cdmiVersion);
    }

    /**
     * Create a factory for CDMI operations with alternate CDMI server.
     * 
     * @param uri
     *            the {@link URI} of the CDMI server.
     * @param cdmiVersion
     *            the version to use in all requests.
     * @return a {@link RequestFactory} object.
     */
    public static RequestFactory newCdmiFactory(URI uri, URI nonCdmiUri,
            String cdmiVersion) {
        return new CdmiRequestFactory(uri, nonCdmiUri, cdmiVersion);
    }

    /**
     * Create a factory for nonCDMI operations.
     * 
     * @param uri
     *            the {@link URI} of the CDMI server.
     * @return a {@link RequestFactory} object.
     */
    public static RequestFactory newNonCdmiFactory(URI uri) {
        return new NonCdmiRequestFactory(uri);
    }

    /**
     * Utility to obtain a nonCDMI factory from a CDMI factory.
     * 
     * @return a {@link RequestFactory} for nonCDMI requests.
     */
    public RequestFactory newNonCdmiFactory() {
        return new NonCdmiRequestFactory(scheme, nonCdmiHost, nonCdmiPort);
    }

    /**
     * Utility to obtain a CDMI factory from a nonCDMI factory.
     * 
     * @param cdmiVersion
     *            the version to use in all requests.
     * @return a {@link RequestFactory} used for CDMI requests.
     */
    public RequestFactory newCdmiFactory(String cdmiVersion) {
        return new CdmiRequestFactory(scheme, host, port, cdmiVersion);
    }
}
