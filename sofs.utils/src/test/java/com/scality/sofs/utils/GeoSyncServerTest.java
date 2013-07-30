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
import java.util.Random;

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
 * TestCase for the GeoSyncServer
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class GeoSyncServerTest {

	protected int port = Config.PORT;
	protected WebConversation wc;
	protected Server server;

	private MockProcessor processor;

	@Before
	public void init() {
		processor = new MockProcessor();
		server = GeoSyncServer.createServer(port, processor);
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

	@Test
	public void hitSimpleSuccess() throws IOException {
		String payLoad = "{ \"Method\" : \"scality\", \"scalitylog\": \"GEOSYNC content\" }";
		WebResponse response = null;
		try {
			response = executeRequest(payLoad);
			assertEquals(200, response.getResponseCode());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertEquals("GEOSYNC content", processor.getLastPayload());
		assertEquals("", response.getText());
	}

	@Test
	public void hitMalformedJson() {
		String payLoad = "{ \"Method\" : \"scality\", \"scalitylog\": GEOSYNC content }";
		try {
			executeRequest(payLoad);
			fail("Should get an exception as it is not a valid json object");

		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (HttpException e) {
			assertEquals(400, e.getResponseCode());
		}
	}

	@Test
	public void hitNotAJsonObject() {
		String payLoad = "sometext";
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
			assertEquals(400, e.getResponseCode());
		}
	}

	@Test
	public void hitNotAJsonContent() {
		String payLoad = "sometext";
		InputStream in = new ByteArrayInputStream(payLoad.getBytes());
		PostMethodWebRequest request = new PostMethodWebRequest(
				"http://localhost:" + port, in, "application/sometype");
		try {
			wc.getResponse(request);
			fail("Unexpected 200 return code");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (SAXException e) {
			e.printStackTrace();
			fail();
		} catch (HttpException e) {
			assertEquals(400, e.getResponseCode());
		}

	}

	@Test
	public void randomTest() {
		String payLoad = payLoadGen("/foo");
		try {
			WebResponse response = executeRequest(payLoad);
			System.out.println(response.getText());
		} catch (IOException e1) {
			e1.printStackTrace();
			fail();
		} catch (SAXException e1) {
			e1.printStackTrace();
			fail();
		}
	}

	private WebResponse executeRequest(String payLoad) throws IOException,
			SAXException {
		return executeRequest(payLoad, port, wc);
	}

	public static WebResponse executeRequest(String payLoad, int port,
			WebConversation wc) throws IOException, SAXException {
		InputStream in = new ByteArrayInputStream(payLoad.getBytes());
		PostMethodWebRequest request = new PostMethodWebRequest(
				"http://localhost:" + port, in, "application/json");

		WebResponse response = wc.getResponse(request);
		return response;
	}

	public static String payLoadGen(String baseDir) {
		Random random = new Random();

		StringBuilder payLoad = new StringBuilder();
		payLoad.append("{ \"Method\" : \"scality\", \"scalitylog\": \"");

		// payload max size is 20
		for (int i = 0; i < 10; i++) {
			payLoad.append("1:"); // version
			payLoad.append(random.nextDouble()).append(":"); // timestamp
			payLoad.append("SOFSPRIVATEKEYTESTVAL")
					.append(random.nextInt(26) + 'a').append(":"); // key
			int ac = random.nextInt(5);
			switch (ac) {
			case 0:
				payLoad.append("MKDIR:::");
				break;
			case 1:
				payLoad.append("MKNOD:::");
				break;
			case 2:
				payLoad.append("ATTR_MODIFIED:::");
				break;
			case 3:
				payLoad.append("MKNOD:::");
				break;
			case 4:
				payLoad.append("CONTENT_MODIFIED:::");
				break;
			default:
				payLoad.append("CONTENT_MODIFIED:::");
				break;
			}
			payLoad.append(baseDir).append("/");

			payLoad.append((char) (random.nextInt(26) + 'a'))
					.append((char) (random.nextInt(26) + 'a'))
					.append((char) (random.nextInt(26) + 'a'));
			payLoad.append("\n");

		}
		payLoad.deleteCharAt(payLoad.length() - 1);
		// End payload
		payLoad.append("\" }");
		return payLoad.toString();
	}

	/*
	 * This class is a test mock, it is definitely not thread-safe And should
	 * not be used out of its context
	 */
	private static class MockProcessor implements GeoSyncPayLoadProcessor {
		String lastPayload = null;

		@Override
		public void process(String payLoad) {
			lastPayload = payLoad;
		}

		public String getLastPayload() {
			return lastPayload;
		}

	}

}
