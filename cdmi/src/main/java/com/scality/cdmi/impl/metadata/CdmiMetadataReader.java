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
package com.scality.cdmi.impl.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.connector.CdmiConnector;

/**
 * 
 * This class can only be used with CDMI operations, not with nonCDMI
 * operations.
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class CdmiMetadataReader {
    private static final String[] META_FIELD_NAMES = { "metadata",
            "valuetransferencoding", "objectName", "objectType", "objectID",
            "parentURI", "capabilitiesURI" };

    private CdmiConnector conn;

    /**
     * Constructor
     * 
     * @param conn
     */
    public CdmiMetadataReader(CdmiConnector conn) {
        this.conn = conn;
    }

    /**
     * @param path
     * @return
     * @throws CdmiConnectionException
     * @throws FileNotFoundException
     */
    public CdmiMetadata readMetadata(String path)
            throws CdmiConnectionException, FileNotFoundException {
        // First get the object type.
        HttpResponse response = conn.readMetadata(path, META_FIELD_NAMES);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            EntityUtils.consumeQuietly(response.getEntity());
            throw new FileNotFoundException(path + " does not exist");
        }
        return extractMetadata(response);
    }

    public String readMetadataValue(HttpResponse response, String key)
            throws CdmiConnectionException {
        try {
            String jsonEncoded = EntityUtils.toString(response.getEntity());
            JsonParser parser = new JsonFactory().createJsonParser(jsonEncoded);
            ObjectMapper m = new ObjectMapper();
            JsonNode root = m.readTree(parser);
            if (root.get("metadata") == null
                    || root.get("metadata").get(key) == null) {
                return null;
            } else {
                return root.get("metadata").get(key).getValueAsText();
            }
        } catch (JsonParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    private CdmiMetadata extractMetadata(HttpResponse response)
            throws CdmiConnectionException {
        try {
            String jsonEncoded = EntityUtils.toString(response.getEntity());
            return new CdmiMetadata(jsonEncoded);
        } catch (ParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }
}
