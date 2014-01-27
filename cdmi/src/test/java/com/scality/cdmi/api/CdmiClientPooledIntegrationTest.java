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

import java.net.URI;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

/**
 * Run the CdmiClientTest against a test server. The server is specified in the
 * "integrationtest.properties" file.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class CdmiClientPooledIntegrationTest extends CdmiClientTest {
    private CdmiConnectionManager cm;
    private Properties prop;

    @Override
    protected CdmiClient getTestClient() {
        return cm.getClient();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        prop = new Properties();
        prop.load(Thread.currentThread().getContextClassLoader()
                .getResource("integrationtest.properties").openStream());
        cm = CdmiConnectionManager.newPooledConnectionManager(
                RequestFactory.newCdmiFactory(
                        URI.create(prop.getProperty("cdmi.server")),
                        URI.create(prop.getProperty("non.cdmi.server")),
                        prop.getProperty("cdmi.version")),
                new CdmiAuthScope(prop.getProperty("cdmi.authscope.host"),
                        Integer.parseInt(prop
                                .getProperty("cdmi.authscope.port"))),
                new CdmiCredentials(prop.getProperty("cdmi.credentials.user"),
                        prop.getProperty("cdmi.credentials.password")),
                new RetryStrategy(Integer.parseInt(prop
                        .getProperty("cdmi.retry.max")), Integer.parseInt(prop
                        .getProperty("cdmi.retry.sleep")), Integer
                        .parseInt(prop.getProperty("cdmi.retry.timeout"))),
                Integer.parseInt(prop.getProperty("cdmi.pool.threads")),
                Integer.parseInt(prop.getProperty("cdmi.blocksize")), Integer
                        .parseInt(prop.getProperty("cdmi.putsize")), Integer
                        .parseInt(prop.getProperty("cdmi.putthreads")));
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        cm.shutdown();
    }
}
