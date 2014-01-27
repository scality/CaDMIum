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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that we parse correctly the responses to CDMI requests.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public class ParsingUtilsTest {
    private ParsingUtils parser;

    @Before
    public void setUp() throws Exception {
        parser = new ParsingUtils();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetValue() throws Exception {
        String json = "{ \"value\": \"foo\" }";
        Assert.assertEquals("foo", parser.extractValueField(json));

        json = "{ \"value1\": \"foo\" ,  \"value2\": \"foo\", \"value\": \"gotit\"}";
        Assert.assertEquals("gotit", parser.extractValueField(json));
    }

    @Test
    public void testGetField() throws Exception {
        String json = "{ \"value1\": \"bar\" ,  \"value2\": \"foo\", \"value\": \"gotit\"}";
        Assert.assertEquals("foo", parser.extractField(json, "value2"));
    }

    @Test
    public void testGetArray() throws Exception {
        String json = "{\"blocks_locations\": [{\"range\": \"0-8388607\","
                + "\"hosts\": [ \"178.33.228.111\", \"176.31.229.85\", \"178.33.237.96\"]}}";
        Assert.assertEquals("0-8388607", parser.extractField(json, "range"));

        String[] hosts = parser.extractArray(json, "hosts");
        Assert.assertEquals(3, hosts.length);
        Assert.assertEquals("178.33.228.111", hosts[0]);
        Assert.assertEquals("176.31.229.85", hosts[1]);
        Assert.assertEquals("178.33.237.96", hosts[2]);
    }
}
