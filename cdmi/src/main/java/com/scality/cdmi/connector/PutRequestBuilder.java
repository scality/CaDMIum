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

import org.apache.http.client.methods.HttpPut;

import com.scality.cdmi.api.CdmiConfigurationException;

/**
 * A class used for building PUT requests easily. This abstraction allows
 * different implementations, especially CDMI and nonCDMI requests.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public interface PutRequestBuilder {
    /**
     * Add contents to the request.
     * 
     * @param contents
     *            a string containing the contents to be included in the
     *            request.
     * @return the builder
     */
    public PutRequestBuilder addContents(String contents);

    /**
     * Add contents to the request.
     * 
     * @param contents
     *            a byte array storing the contents to be included in the
     *            request.
     * @return the builder
     */
    public PutRequestBuilder addContents(byte[] contents);

    /**
     * Add specific metadata to be sent to the CDMI server during the request.
     * 
     * @param key
     *            the key of the metatadata
     * @param value
     *            the value to be stored for this key
     * @return the builder
     */
    public PutRequestBuilder addMetadata(String key, String value);

    /**
     * Add a key / value pair to the body of the request.
     * 
     * @param key
     * @param value
     * @return the builder
     */
    public PutRequestBuilder addBody(String key, String value);

    /**
     * Store all the modifications in the PUT request.
     * 
     * @return a {@link HttpPut} request
     * @throws CdmiConfigurationException
     *             if there was an error processing all the modifications done
     *             in the builder.
     */
    public HttpPut build() throws CdmiConfigurationException;
}
