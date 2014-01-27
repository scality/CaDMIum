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
package com.scality.cdmi.impl.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.scality.cdmi.api.CdmiConnectionException;

/**
 * 
 * Parsing utility class for Json data
 * 
 * @author julien.muller@ezako.com for Scality
 *
 */
public class ParsingUtils {
    private JsonFactory factory;

    /**
     * Default Constructor
     */
    public ParsingUtils() {
        factory = new JsonFactory();
    }

    private String getString(JsonParser jParser, String key) throws JsonParseException, IOException {
        String result = null;
        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jParser.getCurrentName();
            if (key.equals(fieldname)) {
                jParser.nextToken();
                result = jParser.getText();
            }
        }
        return result;
    }

    /**
     * @param entity
     * @return
     * @throws CdmiConnectionException
     */
    public String extractValueField(String entity) throws CdmiConnectionException {
        return extractField(entity, "value");
    }
    
    /**
     * @param entity
     * @param key
     * @return
     * @throws CdmiConnectionException
     */
    public String extractField(String entity, String key) throws CdmiConnectionException {
        try {
            JsonParser jParser = factory.createJsonParser(entity);
            return new String(getString(jParser, key));
        } catch (JsonParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }
    
    /**
     * @param entity
     * @param key
     * @return
     * @throws CdmiConnectionException
     */
    public String[] extractArray(String entity, String key) throws CdmiConnectionException {
        try {
            JsonParser jParser = factory.createJsonParser(entity);
            ArrayList<String> result = new ArrayList<String>();
            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldname = jParser.getCurrentName();
                if (key.equals(fieldname)) {
                    jParser.nextToken();  // Should be JsonToken.START_ARRAY
                    while (jParser.nextToken() != JsonToken.END_ARRAY) {
                        result.add(jParser.getText());
                    }
                }
            }
            return result.toArray(new String[0]);
        } catch (JsonParseException e) {
            throw new CdmiConnectionException(e);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }
}
