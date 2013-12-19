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
package com.scality.cdmi.connector;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;

import com.scality.cdmi.api.CdmiConfigurationException;
import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.api.RequestFactory;
import com.scality.cdmi.api.RetryStrategy;

/**
 * Low level interface to the CDMI server. Handles creating the requests and
 * returns the responses to the client for further processing. Responses
 * entities must always be consumed for the request thread to be freed. See
 * {@link EntityUtils#consume(org.apache.http.HttpEntity)} on how to free the
 * resources associated with a request.
 * 
 * Example use (delete): CdmiConnector connector = new CdmiConnector(
 * RequestFactory.newCdmiFactory( URI.create("http://localhost:80", "1.0.2")),
 * new DefaultHttpClient(), new RetryStrategy()); HttpResponse response =
 * connector.delete("/path/to/my/container");
 * EntityUtils.consume(response.getEntity()); int statusCode =
 * response.getStatusLine().getStatusCode(); return statusCode ==
 * HttpStatus.SC_NO_CONTENT;
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class CdmiConnector {
    private static final String[] OBJECTTYPE = new String[] { "objectType" };
    public static final Log LOG = LogFactory.getLog(CdmiConnector.class);
    private RequestFactory requestFactory;
    private RequestFactory nonCdmiRequestFactory;
    private HttpClient httpClient;
    private RetryStrategy retryStrategy;
    private boolean multiThreaded;

    /**
     * Constructor
     * @param factory
     * @param httpClient
     * @param retryStrategy
     * @param multiThreaded
     */
    public CdmiConnector(RequestFactory factory, HttpClient httpClient, RetryStrategy retryStrategy, boolean multiThreaded) {
        this.requestFactory = factory;
        this.nonCdmiRequestFactory = factory.newNonCdmiFactory();
        this.httpClient = httpClient;
        this.retryStrategy = retryStrategy;
        this.multiThreaded = multiThreaded;
    }

    /**
     * @return true if multi-threaded, false otherwise
     */
    public boolean isMultiThreaded() {
		return multiThreaded;
	}

	/**
	 * @param request
	 * @return
	 * @throws CdmiConnectionException
	 */
	private HttpResponse stubbornExecute(HttpUriRequest request) throws CdmiConnectionException {
        int status = -1;
        HttpResponse response = null;
        int i = retryStrategy.getMaxRetries();
        do {
            if (response != null) {
                // Release connection.
                EntityUtils.consumeQuietly(response.getEntity());
            }
            // FIXME: add some sleep for retries.
            try {
                debugRequest(request);
                response = httpClient.execute(request);
                debugResponse(response);
            } catch (ClientProtocolException e) {
                throw new CdmiConnectionException(e);
            } catch (ConnectTimeoutException e) {
                --i;
                continue;
            } catch (IOException e) {
                throw new CdmiConnectionException(e);
            }
            status = response.getStatusLine().getStatusCode();
        } while (HttpStatus.SC_INTERNAL_SERVER_ERROR == status && --i > 0);
        if (-1 == status) {
            throw new CdmiConnectionException("Cannot connect to server. All requests timed out.");
        } else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == status) {
            throw new CdmiConnectionException(
                    "Can't seem to get any response from server. Got response "
                            + response.getStatusLine());
        }
        return response;
    }

    /**
     * Common operations.
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse discoverCapabilities() throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGet(CdmiTypes.CDMI_CAPABILITY,
                    CdmiURI.DISCOVER_CAPABILITIES);
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    private String concatFieldNames(String[] fields) {
        StringBuffer buffer = new StringBuffer();
        for (String field : fields) {
            buffer.append(field).append(";");
        }
        if (buffer.length() > 0) {
            buffer.delete(buffer.length() - 1, buffer.length());
        }
        return buffer.toString();
    }

    /**
     * Simple container operations.
     * @param containerPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse createContainer(String containerPath) throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory.newPut(CdmiTypes.CDMI_CONTAINER, containerPath).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param containerPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse listContainer(String containerPath) throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGet(containerPath, "children");
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param srcContainerPath
     * @param destContainerPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse moveContainer(String srcContainerPath, String destContainerPath)
            throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory.newPut(CdmiTypes.CDMI_CONTAINER, destContainerPath)
                    .addBody("move", RequestFactory.URIEscapeString(srcContainerPath)).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        } catch (ParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    public HttpResponse setContainerMetadata(String path, String key,
            String value) throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory
                    .newPut(CdmiTypes.CDMI_CONTAINER, path, "metadata:" + key)
                    .addBody("metadata",
                            String.format("{\"%s\":\"%s\"}", key, value))
                    .build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param path
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse delete(String path) throws CdmiConnectionException {
        try {
            HttpDelete delete = requestFactory.newDelete(path);
            return stubbornExecute(delete);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param path
     * @param metadatafields
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse readMetadata(String path, String[] metadatafields)
            throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGet(path, concatFieldNames(metadatafields));
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * Create an empty object
     * @param dataObjectPath
     * @param binary
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse createEmptyObject(String dataObjectPath, boolean binary)
            throws CdmiConnectionException {
        try {
            HttpPut put;
            if (binary) {
                put = requestFactory.newPut(CdmiTypes.CDMI_OBJECT, dataObjectPath)
                        .addContents(new byte[] {}).build();
            } else {
                put = requestFactory.newPut(CdmiTypes.CDMI_OBJECT, dataObjectPath).addContents("")
                        .build();
            }
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param dataObjectPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse createEmptyObjectNonCdmi(String dataObjectPath)
            throws CdmiConnectionException {
        try {
            HttpPut put = nonCdmiRequestFactory.newPut(CdmiTypes.CDMI_OBJECT, dataObjectPath)
                    .addContents(new byte[0]).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }

    }

    /**
     * @param srcObjectPath
     * @param destObjectPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse copyObject(String srcObjectPath, String destObjectPath)
            throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory.newPut(CdmiTypes.CDMI_OBJECT, destObjectPath)
                    .addBody("copy", RequestFactory.URIEscapeString(srcObjectPath)).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param srcObjectPath
     * @param destObjectPath
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse moveObject(String srcObjectPath, String destObjectPath)
            throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory.newPut(CdmiTypes.CDMI_OBJECT, destObjectPath)
                    .addBody("move", RequestFactory.URIEscapeString(srcObjectPath)).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        } catch (ParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }
    
    /**
     * @param dataObjectPath
     * @param offset
     * @param length
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse readObject(String dataObjectPath, long offset, long length)
            throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGetWithRange(dataObjectPath, offset, length);
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param dataObjectPath
     * @param offset
     * @param length
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse readObjectNonCdmi(String dataObjectPath, long offset, long length)
            throws CdmiConnectionException {
        try {
            HttpGet get = nonCdmiRequestFactory.newGetWithRange(dataObjectPath, offset, length);
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param dataObjectPath
     * @param offset
     * @param length
     * @param binaryData
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse updateObject(String dataObjectPath, long offset, long length,
            byte[] binaryData) throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory
                    .newPutWithRange(CdmiTypes.CDMI_OBJECT, dataObjectPath, offset, length)
                    .addContents(binaryData).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param dataObjectPath
     * @param offset
     * @param length
     * @param utf8Data
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse updateObject(String dataObjectPath, long offset, long length,
            String utf8Data) throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory
                    .newPutWithRange(CdmiTypes.CDMI_OBJECT, dataObjectPath, offset, length)
                    .addContents(utf8Data).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param dataObjectPath
     * @param offset
     * @param length
     * @param data
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse updateObjectNonCdmi(String dataObjectPath, long offset, long length,
            byte[] data) throws CdmiConnectionException {
        try {
            HttpPut put = nonCdmiRequestFactory
                    .newPutWithRange(CdmiTypes.CDMI_OBJECT, dataObjectPath, offset, length)
                    .addContents(data).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * @param path
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse getObjectType(String path) throws CdmiConnectionException {
        return readMetadata(path, OBJECTTYPE);
    }

    /**
     * @param key
     * @param query
     * @return
     * @throws CdmiConnectionException
     */
    public HttpResponse userSpecificQuery(String key, String query) throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGet(key, query);
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * Scality specific operation for flushing out a data object.
     * 
     * @param request
     */
    public HttpResponse forceFlush(String dataObjectPath, long size)
            throws CdmiConnectionException {
        try {
            HttpPut put = nonCdmiRequestFactory
                    .newPut(CdmiTypes.CDMI_OBJECT, dataObjectPath)
                    .setHeader("X-Scal-Truncate", String.valueOf(size)).build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    public HttpResponse setObjectMetadata(String path, String key, String value)
            throws CdmiConnectionException {
        try {
            HttpPut put = requestFactory
                    .newPut(CdmiTypes.CDMI_OBJECT, path, "metadata:" + key)
                    .addBody("metadata",
                            String.format("{\"%s\":\"%s\"}", key, value))
                    .build();
            return stubbornExecute(put);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    public HttpResponse getMetadataValue(String path, String key)
            throws CdmiConnectionException {
        try {
            HttpGet get = requestFactory.newGet(path,
                    String.format("metadata:%s", key));
            return stubbornExecute(get);
        } catch (CdmiConfigurationException e) {
            throw new CdmiConnectionException(e);
        }
    }

    private void debugRequest(HttpRequest request) {
        LOG.debug("Request: " + request.toString());
    }

    private void debugResponse(HttpResponse response) {
        LOG.debug("Response: " + response.toString());
    }

}
