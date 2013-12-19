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

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.scality.cdmi.api.CdmiConfigurationException;
import com.scality.cdmi.connector.PutRequestBuilder;

/**
 * @author julien.muller@ezako.com for Scality
 *
 */
public class CdmiPutRequestBuilder implements PutRequestBuilder {
    private HttpPut put;
    private TreeMap<String, String> entity;
    private TreeMap<String, String> metadata;

    public CdmiPutRequestBuilder(HttpPut put) {
        this.put = put;
        this.entity = new TreeMap<String, String>();
        this.metadata = new TreeMap<String, String>();
    }
    
    @Override
    public PutRequestBuilder addContents(String contents) {
        entity.put("valuetransferencoding", "utf-8");
        entity.put("value", contents);
        return this;
    }

    @Override
    public PutRequestBuilder addContents(byte[] contents) {
        entity.put("valuetransferencoding", "base64");
        String encodedValue = new String(Base64.encodeBase64(contents));
        entity.put("value", encodedValue);
        return this;
    }
    
    @Override
    public PutRequestBuilder addMetadata(String key, String value) {
        metadata.put(key, value);
        return this;
    }

    @Override
    public PutRequestBuilder addBody(String key, String value) {
        entity.put(key, value);
        return this;
    }

    private static String formatMap(Map<String, String> entries) {
        boolean hasValue = false;
        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        for (Map.Entry<String, String> item : entries.entrySet()) {
            if (item.getValue().startsWith("{")) {
                // Embedded JSON object.
                buffer.append(String.format(" \"%s\": %s,", item.getKey(),
                        item.getValue()));
            } else {
                buffer.append(String.format(" \"%s\": \"%s\",", item.getKey(),
                        item.getValue()));
            }
            hasValue = true;
        }
        if (hasValue) {
            buffer.delete(buffer.length() - 1, buffer.length());
        }
        buffer.append('}');
        return buffer.toString();
    }

    @Override
    public HttpPut build() throws CdmiConfigurationException {
        if (!metadata.isEmpty()) {
            entity.put("metadata", formatMap(metadata));
        }
        try {
            put.setEntity(new StringEntity(formatMap(entity)));
        } catch (UnsupportedEncodingException e) {
            throw new CdmiConfigurationException(e);
        }
        return put;
    }

    @Override
    public PutRequestBuilder setHeader(String key, String value) {
        put.setHeader(key, value);
        return this;
    }
}
