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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.scality.cdmi.api.CdmiConfigurationException;
import com.scality.cdmi.api.RequestFactory;

/**
 * Test that the {@link CdmiRequestFactory} generates the desired requests.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class CdmiRequestFactoryTest {
	
    /**
     * The underlying factory.
     */
    private RequestFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = RequestFactory.newCdmiFactory(new URI("http://foo.com:8080"), "6");
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
        Assert.assertEquals("testcontainer", put.getFirstHeader("Accept").getValue());
        Assert.assertEquals("testcontainer", put.getFirstHeader("Content-Type").getValue());
        Assert.assertEquals("6", put.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test a put with a query.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testPutWithQuery() throws CdmiConfigurationException {
        HttpPut put = factory.newPut("testcontainer", "bar/baz/cat", "one=two").build();
        Assert.assertEquals("http://foo.com:8080/bar/baz/cat?one=two", put.getURI().toString());
        Assert.assertEquals("testcontainer", put.getFirstHeader("Accept").getValue());
        Assert.assertEquals("testcontainer", put.getFirstHeader("Content-Type").getValue());
        Assert.assertEquals("6", put.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test a put with a value range.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testPutWithRange() throws CdmiConfigurationException {
        HttpPut put = factory.newPutWithRange("testobject", "bar/foo.txt", 32, 29).build();
        Assert.assertEquals("http://foo.com:8080/bar/foo.txt?value:32-60", put.getURI().toString());
        Assert.assertEquals("testobject", put.getFirstHeader("Accept").getValue());
        Assert.assertEquals("testobject", put.getFirstHeader("Content-Type").getValue());
        Assert.assertEquals("6", put.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test the simple Get request.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testSimpleGet() throws CdmiConfigurationException {
        HttpGet get = factory.newGet("foo");
        Assert.assertEquals("http://foo.com:8080/foo", get.getURI().toString());
        Assert.assertNull(get.getFirstHeader("Content-Type"));
        Assert.assertEquals("6", get.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test a Get with a query.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testGetWithQuery() throws CdmiConfigurationException {
        HttpGet get = factory.newGet("bar/baz/cat", "one=two");
        Assert.assertEquals("http://foo.com:8080/bar/baz/cat?one=two", get.getURI().toString());
        Assert.assertNull(get.getFirstHeader("Content-Type"));
        Assert.assertEquals("6", get.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test a Get with a value range.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testGetWithRange() throws CdmiConfigurationException {
        HttpGet get = factory.newGetWithRange("bar/foo.txt", 32, 29);
        Assert.assertEquals("http://foo.com:8080/bar/foo.txt?value:32-60", get.getURI().toString());
        Assert.assertNull(get.getFirstHeader("Content-Type"));
        Assert.assertEquals("6", get.getFirstHeader("X-CDMI-Specification-Version").getValue());
    }

    /**
     * Test a simple Delete.
     * 
     * @throws CdmiConfigurationException
     */
    @Test
    public void testDelete() throws CdmiConfigurationException {
        HttpDelete delete = factory.newDelete("foo/");
        Assert.assertEquals("http://foo.com:8080/foo/", delete.getURI().toString());
        Assert.assertEquals("6", delete.getFirstHeader("X-CDMI-Specification-Version").getValue());
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
