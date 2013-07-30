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
package com.scality.sofs.utils.watch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.scality.sofs.utils.Config;
import com.scality.sofs.utils.GeoSyncServer;
import com.scality.sofs.utils.GeoSyncServerTest;
import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.events.SofsEventTypes;

/**
 * @author ziad.bizri@ezako.com for Scality
 * @since 1.7
 */
public class WatcherIntegrationTest {
	final int port = Config.PORT;

	/**
	 * This test runs a real server and system outs but, does not really check
	 * its results.
	 * 
	 * TODO validate the test result
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void realistic() throws IOException, SAXException {

		final SofsWatchService watcher = Factory.createService();

		final List<SofsEvent> results = new CopyOnWriteArrayList<SofsEvent>();

		Server server = GeoSyncServer.createServer(port,
				Factory.createProcessor(watcher));
		System.out.println("Running server on port " + port + "...");

		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Path p1 = Factory.createPath("/foo");
		p1.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);
		Path p2 = Factory.createPath("/foo/include");
		p2.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						WatchKey nextKey = watcher.poll(100,
								TimeUnit.MILLISECONDS);

						// We are only sysouting the event, implementations can
						// use the nextEvent object to do much more
						if (nextKey != null) {
							// Assert.assertEquals(key, nextKey);
							List<WatchEvent<?>> events = nextKey.pollEvents();

							if (events.size() == 0)
								Assert.fail("We should have an event");
							for (WatchEvent<?> event : events) {
								System.out.println(event);
								// get the sofsevent
								SofsEvent sofsEvent = ((SofsWatchEvent) event)
										.getOriginalEvent();
								System.out.println(sofsEvent);
								results.add(sofsEvent);
							}

						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		String payLoad = "{ \"Method\" : \"scality\", \"scalitylog\": \""
				+ "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000020:MKDIR:::/foo/include\n"
				+ "1:1364220648.847604:32D745056A8E9D6943CBF0000000010200000021:MKNOD:::/foo/include/paths.h\n"
				+ "1:1364220648.848787:32D745056A8E9D6943CBF0000000010200000022:ATTR_MODIFIED:::/foo/include/paths.h\n"
				+ "1:1364220648.881096:0F754106F388BFABF4B750000000010200000023:MKNOD:::/foo/include/unctrl.h\n"
				+ "1:1364220648.860763:32D745056A8E9D6943CBF0000000010200000024:CONTENT_MODIFIED:::/foo/include/paths.h"
				+ "\" }";

		WebConversation wc = new WebConversation();
		InputStream in = new ByteArrayInputStream(payLoad.getBytes());
		PostMethodWebRequest request = new PostMethodWebRequest(
				"http://localhost:" + port, in, "application/json");

		WebResponse response = wc.getResponse(request);
		Assert.assertEquals(200, response.getResponseCode());

		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertEquals(new SofsEvent(1, 1364220640.307028,
				"889410000CB25F0CC9CF43000000010200000020",
				SofsEventTypes.MKDIR, "/foo/include", null), results.get(0));
		Assert.assertEquals(new SofsEvent(1, 1364220648.847604,
				"32D745056A8E9D6943CBF0000000010200000021",
				SofsEventTypes.MKNOD, "/foo/include/paths.h", null), results
				.get(1));
		Assert.assertEquals(new SofsEvent(1, 1364220648.848787,
				"32D745056A8E9D6943CBF0000000010200000022",
				SofsEventTypes.ATTR_MODIFIED, "/foo/include/paths.h", null),
				results.get(2));
		Assert.assertEquals(new SofsEvent(1, 1364220648.881096,
				"0F754106F388BFABF4B750000000010200000023",
				SofsEventTypes.MKNOD, "/foo/include/unctrl.h", null), results
				.get(3));
		Assert.assertEquals(new SofsEvent(1, 1364220648.860763,
				"32D745056A8E9D6943CBF0000000010200000024",
				SofsEventTypes.CONTENT_MODIFIED, "/foo/include/paths.h", null),
				results.get(4));

		System.out.println("success");
	}

	@Test
	public void genTest() {

		final String baseDir = "/bar";

		// Create watcher
		final SofsWatchService watcher = Factory.createService();

		Server server = GeoSyncServer.createServer(port,
				Factory.createProcessor(watcher));
		System.out.println("Running server on port " + port + "...");
		try {
			server.start();
			System.out.println("Server started");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		Path foo = Factory.createPath(baseDir);
		try {
			foo.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}

		try {
			String payLoad = GeoSyncServerTest.payLoadGen(baseDir);
			System.out.println(payLoad);
			WebResponse response = GeoSyncServerTest.executeRequest(payLoad,
					port, new WebConversation());
			if (response.getResponseCode() != 200)
				Assert.fail();
			System.out.println(response.getText());
			System.out.println(response.getContentType());
			System.out.println("success");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (SAXException e) {
			e.printStackTrace();
			Assert.fail();
		}

		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
