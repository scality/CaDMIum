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

import org.apache.http.client.HttpClient;

import com.scality.cdmi.impl.CdmiBasicConnectionManager;
import com.scality.cdmi.impl.CdmiPooledConnectionManager;

/**
 * A class used for managing the connections to the CDMI server.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public abstract class CdmiConnectionManager {
    /**
     * Get a cdmi client for communicating with the CDMI server.
     * 
     * @return a {@link CdmiClient} object.
     */
    public abstract CdmiClient getClient();
    
    /**
     * Get an http client for other communications. This should only
     * be used for tests.
     * 
     * @return a {@link HttpClient} object.
     */
    public abstract HttpClient getHttpClient();

    /**
     * Shutdown all the communications with the CDMI server and close all the
     * connections.
     */
    public abstract void shutdown();

    /**
     * Create a basic connection manager: all the requests are synchronous and
     * run on a single threaded.
     * 
     * @param factory
     *            the {@link RequestFactory} used for generating the requests to
     *            the CDMI server.
     * @param authscope
     *            the {@link CdmiAuthScope} used in user authentication scope.
     * @param credentials
     *            the {@link CdmiCredentials} used in the basic HTTP
     *            authentication.
     * @param retryStrategy
     *            the {@link RetryStrategy} used during the communication with
     *            the CDMI server.
     * @param ioBufferSize
     *            the size of the intermediate buffer used for reading and
     *            writing.
     * @param maxPutSize
     *            the maximum length of a PUT request.
     * @return a {@link CdmiConnectionManager} object.
     */
    public static CdmiBasicConnectionManager newBasicConnectionManager(RequestFactory factory,
            CdmiAuthScope authscope, CdmiCredentials credentials, RetryStrategy retryStrategy,
            int ioBufferSize, int maxPutSize) {
        return new CdmiBasicConnectionManager(factory, authscope, credentials, retryStrategy,
                ioBufferSize, maxPutSize);
    }

    /**
     * Create a basic connection manager: all the requests are synchronous and
     * run on a single threaded. Usable default values are used for other
     * parameters.
     * 
     * @param factory
     *            the {@link RequestFactory} used for generating the requests to
     *            the CDMI server.
     * @param authscope
     *            the {@link CdmiAuthScope} used in user authentication scope.
     * @param credentials
     *            the {@link CdmiCredentials} used in the basic HTTP
     *            authentication.
     * @return a {@link CdmiConnectionManager} object.
     */
    public static CdmiBasicConnectionManager newBasicConnectionManager(RequestFactory factory,
            CdmiAuthScope authscope, CdmiCredentials credentials) {
        return new CdmiBasicConnectionManager(factory, authscope, credentials, new RetryStrategy(),
                8192, 16777216);
    }

    /**
     * Create a pooled connection manager: all the requests are synchronous but
     * can run on different threads.
     * 
     * @param factory
     *            the {@link RequestFactory} used for generating the requests to
     *            the CDMI server.
     * @param authscope
     *            the {@link CdmiAuthScope} used in user authentication scope.
     * @param credentials
     *            the {@link CdmiCredentials} used in the basic HTTP
     *            authentication.
     * @param retryStrategy
     *            the {@link RetryStrategy} used during the communication with
     *            the CDMI server.
     * @param max_total_connection
     *            the maximum number of threads
     * @param ioBufferSize
     *            the size of the intermediate buffer used for reading and
     *            writing.
     * @param maxPutSize
     *            the maximum length of a PUT request.
     * @return a {@link CdmiConnectionManager} object.
     */
    public static CdmiPooledConnectionManager newPooledConnectionManager(RequestFactory factory,
            CdmiAuthScope authscope, CdmiCredentials credentials, RetryStrategy retryStrategy,
            int max_total_connection, int ioBufferSize, int maxPutSize, int maxPutThreads) {
        return new CdmiPooledConnectionManager(factory, authscope, credentials, retryStrategy,
                max_total_connection, ioBufferSize, maxPutSize, maxPutThreads);
    }

    /**
     * Create a pooled connection manager: all the requests are synchronous but
     * can run on different threads. Usable default values are used for other
     * parameters.
     * 
     * @param factory
     *            the {@link RequestFactory} used for generating the requests to
     *            the CDMI server.
     * @param authscope
     *            the {@link CdmiAuthScope} used in user authentication scope.
     * @param credentials
     *            the {@link CdmiCredentials} used in the basic HTTP
     *            authentication.
     * @return a {@link CdmiConnectionManager} object.
     */
    public static CdmiPooledConnectionManager newPooledConnectionManager(RequestFactory factory,
            CdmiAuthScope authscope, CdmiCredentials credentials) {
        return new CdmiPooledConnectionManager(factory, authscope, credentials,
                new RetryStrategy(), 20, 8192, 16777216, 5);
    }
}
