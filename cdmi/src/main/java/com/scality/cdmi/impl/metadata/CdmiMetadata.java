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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.connector.CdmiTypes;

/**
 * @author ziad.bizri@ezako.com for Scality
 *
 */
public class CdmiMetadata {
    private final long size;
    private final long mtime;
    private final long atime;
    private final String valuetransferencoding;
    private final String objectName;
    private final String objectType;
    private final String objectID;
    private final String parentURI;
    private final String capabilitiesURI;
    private final boolean isContainer;
    // Should not be static since SimpleDateFormat is not thread safe
    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.000000'Z'");

    /**
     * Constructor
     * @param metadata
     * @throws CdmiConnectionException
     */
    public CdmiMetadata(String metadata) throws CdmiConnectionException {
        try {
            JsonParser parser = new JsonFactory().createJsonParser(metadata);
            ObjectMapper m = new ObjectMapper();
            JsonNode root = m.readTree(parser);
            mtime = DATE_FORMATTER.parse(root.get("metadata").get("cdmi_mtime").getTextValue())
                    .getTime() / 1000;
            atime = DATE_FORMATTER.parse(root.get("metadata").get("cdmi_atime").getTextValue())
                    .getTime() / 1000;
            objectName = root.get("objectName").getTextValue().replace("<dot>", ".");
            objectType = root.get("objectType").getTextValue();
            objectID = root.get("objectID").getTextValue();
            //parentURI = (root.has("parentURI")) ? root.get("parentURI").getTextValue() : "";
            
            JsonNode parentUriNode = root.get("parentURI");
            if(parentUriNode != null) {
                parentURI = parentUriNode.getTextValue().replace("<dot>", ".");
            } else {
                parentURI = "";
            }
            
            capabilitiesURI = root.get("capabilitiesURI").getTextValue();
            if (objectType.equals(CdmiTypes.CDMI_OBJECT)) {
                valuetransferencoding = root.get("valuetransferencoding").getTextValue();
                size = root.get("metadata").get("cdmi_size").getLongValue();
                isContainer = false;
            } else {
                valuetransferencoding = "";
                size = -1L;
                isContainer = true;
            }
        } catch (ParseException e) {
            throw new CdmiConnectionException("Internal server error ", e);
        } catch (JsonParseException e) {
            throw new CdmiConnectionException("Internal server error ", e);
        } catch (JsonProcessingException e) {
            throw new CdmiConnectionException("Internal server error ", e);
        } catch (IOException e) {
            throw new CdmiConnectionException("Internal server error ", e);
        }
    }

    /**
     * @return
     */
    public boolean isContainer() {
        return isContainer;
    }

    /**
     * @return
     */
    public long getSize() {
        return size;
    }

    /**
     * @return
     */
    public String getValuetransferencoding() {
        return valuetransferencoding;
    }

    /**
     * @return
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @return
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * @return
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * @return
     */
    public String getParentURI() {
        return parentURI;
    }

    /**
     * @return
     */
    public String getCapabilitiesURI() {
        return capabilitiesURI;
    }

    /**
     * @return
     */
    public long getMtime() {
        return mtime;
    }

    /**
     * @return
     */
    public long getAtime() {
        return atime;
    }

    /**
     * @return
     */
    public String fullPath() {
        return parentURI + objectName;
    }

    @Override
    public String toString() {
        return "CdmiMetadata [fullpath = " + fullPath() + ", size=" + size + ", mtime=" + mtime
                + ", atime=" + atime + ", valuetransferencoding=" + valuetransferencoding
                + ", objectName=" + objectName + ", objectType=" + objectType + ", objectID="
                + objectID + ", parentURI=" + parentURI + ", capabilitiesURI=" + capabilitiesURI
                + ", isContainer=" + isContainer + "]";
    }
}
