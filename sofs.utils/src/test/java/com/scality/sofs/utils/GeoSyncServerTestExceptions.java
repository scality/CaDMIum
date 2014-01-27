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
package com.scality.sofs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * TestCase for the GeoSyncServer throwing exceptions
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class GeoSyncServerTestExceptions {

	protected int port = Config.PORT;
	protected WebConversation wc;
	protected Server server;

	@Before
	public void init() {
		server = GeoSyncServer.createServer(port, new ThrowProcessor());
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		wc = new WebConversation();
	}

	@After
	public void tearsDown() {
		if (server.isStarted())
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	private WebResponse executeRequest(String payLoad) throws IOException,
			SAXException {
		InputStream in = new ByteArrayInputStream(payLoad.getBytes());
		PostMethodWebRequest request = new PostMethodWebRequest(
				"http://localhost:" + port, in, "application/json");

		WebResponse response = wc.getResponse(request);
		return response;
	}

	/*
	 * This class is a test mock
	 */
	private static class ThrowProcessor implements GeoSyncPayLoadProcessor {
		@Override
		public void process(String payLoad)
				throws GeoSyncPayLoadProcessingException {
			throw new GeoSyncPayLoadProcessingException("Always throwing");
		}
	}

	@Test
	public void hitThrowingProcessor() throws Exception {
		String payLoad = "{ \"Method\" : \"scality\", \"scalitylog\": \"GEOSYNC content\" }";
		try {
			executeRequest(payLoad);
			fail("Should get an exception as it is not a json object");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (HttpException e) {
			assertEquals(500, e.getResponseCode());
		}
	}
}
