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

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import com.scality.cdmi.api.CdmiConfigurationException;

/**
 * Test that the put requests that are generated are what is expected.
 *
 * @author ziad.bizri@ezako.com for Scality
 *
 */
public class CdmiPutRequestBuilderTest {
	
    private CdmiPutRequestBuilder newBuilder() {
        return new CdmiPutRequestBuilder(new HttpPut("http://foo"));
    }

    /**
     * Test that if we add a string contents, the value encoding is utf-8
     * 
     * @throws CdmiConfigurationException
     * @throws IOException
     */
    @Test
    public void testAddStringContents() throws CdmiConfigurationException, IOException {
        CdmiPutRequestBuilder builder = newBuilder();
        HttpPut put = builder.addContents("foo").build();
        
        JsonParser parser = new JsonFactory().createJsonParser(EntityUtils.toString(put.getEntity()));
        JsonNode root = new ObjectMapper().readTree(parser);
        Assert.assertEquals("utf-8", root.get("valuetransferencoding").getTextValue());
        Assert.assertEquals("foo", root.get("value").getTextValue());
        Assert.assertFalse(root.get("metadata") != null);
    }

    /**
     * Test that binary contents is automatically encoded in Base64
     * 
     * @throws CdmiConfigurationException
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     */
    @Test
    public void testAddBinaryContents() throws CdmiConfigurationException, IOException {
        CdmiPutRequestBuilder builder = newBuilder();
        HttpPut put = builder.addContents(new byte[] { 34, 35, 36 }).build();
        JsonParser parser = new JsonFactory().createJsonParser(EntityUtils.toString(put.getEntity()));
        JsonNode root = new ObjectMapper().readTree(parser);
        Assert.assertEquals("base64", root.get("valuetransferencoding").getTextValue());
        Assert.assertEquals("IiMk", root.get("value").getTextValue());
        Assert.assertFalse(root.get("metadata") != null);
    }

    /**
     * Test that metadata is automatically added.
     * 
     * @throws CdmiConfigurationException
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testAddMetadata() throws CdmiConfigurationException, IOException {
        CdmiPutRequestBuilder builder = newBuilder();
        HttpPut put = builder.addContents(new byte[] { 34, 35, 36 }).addMetadata("bar", "baz")
                .build();
        JsonParser parser = new JsonFactory().createJsonParser(EntityUtils.toString(put.getEntity()));
        JsonNode root = new ObjectMapper().readTree(parser);
        Assert.assertEquals("base64", root.get("valuetransferencoding").getTextValue());
        Assert.assertEquals("IiMk", root.get("value").getTextValue());
        Assert.assertEquals("baz", root.get("metadata").get("bar").getTextValue());
    }

    /**
     * Test that body is automatically added.
     * 
     * @throws CdmiConfigurationException
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testAddBody() throws CdmiConfigurationException, IOException {
        CdmiPutRequestBuilder builder = newBuilder();
        HttpPut put = builder.addContents(new byte[] { 34, 35, 36 }).addBody("bar", "baz").build();
        JsonParser parser = new JsonFactory().createJsonParser(EntityUtils.toString(put.getEntity()));
        JsonNode root = new ObjectMapper().readTree(parser);
        Assert.assertEquals("base64", root.get("valuetransferencoding").getTextValue());
        Assert.assertEquals("IiMk", root.get("value").getTextValue());
        Assert.assertEquals("baz", root.get("bar").getTextValue());
    }
}