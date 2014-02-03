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

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.scality.cdmi.api.CdmiConfigurationException;
import com.scality.cdmi.api.RequestFactory;

/**
 * Test that the {@link NonCdmiRequestFactory} generates the desired requests.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class NonCdmiRequestFactoryTest {
    /**
     * The underlying factory.
     */
    private RequestFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = RequestFactory.newNonCdmiFactory(new URI("http://foo.com:8080"));
    }

    /**
     * Test the simple put request.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testSimplePut() throws CdmiConfigurationException {
        HttpPut put = factory.newPut("testcontainer", "foo").build();
        Assert.assertEquals("http://foo.com:8080/foo", put.getURI().toString());
        Assert.assertEquals("application/octet-stream", put.getFirstHeader("Content-Type").getValue());
    }

    /**
     * Test a put with a query.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testPutWithQuery() throws CdmiConfigurationException {
        boolean errorRaised = false;
        try {
            factory.newPut("testcontainer", "bar/baz/cat", "one=two").build();
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }
        Assert.assertTrue(errorRaised);
    }

    /**
     * Test a put with a value range.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testPutWithRange() throws CdmiConfigurationException {
        HttpPut put = factory.newPutWithRange("testobject", "bar/foo.txt", 32, 29).build();
        Assert.assertEquals("http://foo.com:8080/bar/foo.txt", put.getURI().toString());
        Assert.assertEquals("bytes=32-60", put.getFirstHeader("Content-Range").getValue());
        Assert.assertEquals("application/octet-stream", put.getFirstHeader("Content-Type").getValue());
    }

    /**
     * Test the simple Get request.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testSimpleGet() throws CdmiConfigurationException {
        boolean errorRaised = false;
        try {
            factory.newGet("foo");
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }
        Assert.assertTrue(errorRaised);
    }

    /**
     * Test a Get with a query.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testGetWithQuery() throws CdmiConfigurationException {
        boolean errorRaised = false;
        try {
            factory.newGet("bar/baz/cat", "one=two");
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }
        Assert.assertTrue(errorRaised);
    }

    /**
     * Test a Get with a value range.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testGetWithRange() throws CdmiConfigurationException {
        HttpGet get = factory.newGetWithRange("bar/foo.txt", 32, 29);
        Assert.assertEquals("http://foo.com:8080/bar/foo.txt", get.getURI().toString());
        Assert.assertNull(get.getFirstHeader("Content-Type"));
        Assert.assertEquals("bytes=32-60", get.getFirstHeader("Range").getValue());
    }

    /**
     * Test a simple Delete.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testDelete() throws CdmiConfigurationException {
        boolean errorRaised = false;
        try {
            factory.newDelete("foo/");
        } catch (UnsupportedOperationException e) {
            errorRaised = true;
        }
        Assert.assertTrue(errorRaised);
    }


    /**
     * Test a simple Head.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testHead() throws CdmiConfigurationException {
        HttpHead head = factory.newHead("foo/");
        Assert.assertEquals("http://foo.com:8080/foo/", head.getURI().toString());
    }
}
