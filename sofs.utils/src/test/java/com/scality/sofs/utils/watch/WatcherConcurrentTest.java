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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.scality.sofs.utils.Config;
import com.scality.sofs.utils.GeoSyncServer;
import com.scality.sofs.utils.GeoSyncServerTest;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 */
public class WatcherConcurrentTest {
	static String baseDir = "/foo";
	static int port = Config.PORT;

	private SofsWatchService watcher;
	private Server server;

	@Before
	public void setUp() {
		watcher = Factory.createService();
		server = GeoSyncServer.createServer(port, 26,
				Factory.createProcessor(watcher));

		System.out.println("Running server on port " + port + "...");
		try {
			server.start();
			System.out.println("Server started");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@After
	public void tearDown() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unable to stop server");
		}
	}

	@Test
	public void heavyLoad() {
		loadRun(25, 1500, 20);
	}

	@Test
	public void simpleLoad() {
		loadRun(1, 1, 1);
	}

	@Test
	public void basicLoad() {
		loadRun(5, 10, 2);
	}

	@Test
	public void normalLoad() {
		loadRun(10, 100, 5);
	}

	private void loadRun(int producerThreads, int producerTry, int consumThread) {
		// Create watcher
		Path foo = Factory.createPath(baseDir);
		try {
			foo.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		Set<WatchEvent<?>> result = Collections
				.newSetFromMap(new ConcurrentHashMap<WatchEvent<?>, Boolean>());

		// Should be less than the thread pool size of server
		// Also a limit on client ports available
		List<Thread> threads = createProducers(producerThreads, producerTry, 1);

		AtomicLong nb = new AtomicLong();
		for (Thread t : createConsum(watcher, consumThread, 20, result, nb))
			t.start();

		for (Thread t : threads)
			t.start();

		try {
			for (Thread t : threads)
				t.join();
			// One second wait for the consumer to finish
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}

		System.out.println("Result:");
		System.out.println(result.size() + " events watched of "
				+ (10 * producerThreads * producerTry));
		System.out.println("result count: " + nb.longValue());
		assertEquals(
				"Some events were lost, you probably have a multithreading issue ...",
				result.size(), 10 * producerThreads * producerTry);
	}

	private static List<Thread> createConsum(final SofsWatchService watcher,
			final int nbThreads, final int sleep,
			final Set<WatchEvent<?>> result, final AtomicLong nb) {
		List<Thread> threads = new ArrayList<Thread>();
		for (int t = 0; t < nbThreads; t++) {
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							WatchKey k = watcher.poll(sleep,
									TimeUnit.MILLISECONDS);
							if (k != null) {
								for (WatchEvent<?> we : k.pollEvents()) {
									result.add(we);
									nb.incrementAndGet();
								}
								k.reset();
							}
							// Thread.sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
							fail();
							System.exit(1);
						} catch (NullPointerException npe) {
							npe.printStackTrace();
							fail();
							System.exit(1);
						}

					}
				}
			});
			threads.add(th);
		}
		return threads;
	}

	private static List<Thread> createProducers(final int nbThreads,
			final int nbTry, final int sleep) {
		List<Thread> threads = new ArrayList<Thread>();

		for (int t = 0; t < nbThreads; t++) {
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					int i = 0;
					WebConversation wc = new WebConversation();
					InputStream in;

					while (i++ < nbTry) {
						String payLoad = GeoSyncServerTest.payLoadGen(baseDir);
						try {

							in = new ByteArrayInputStream(payLoad.getBytes());
							PostMethodWebRequest request = new PostMethodWebRequest(
									"http://127.0.0.1:" + port, in,
									"application/json");

							wc.getResponse(request);

							Thread.sleep(sleep);
						} catch (HttpException e) {
							System.out.println(payLoad);
							e.printStackTrace();
							fail();
						} catch (InterruptedException e) {
							e.printStackTrace();
							fail();
						} catch (IOException e) {
							System.out.println(payLoad);
							e.printStackTrace();
							fail();
						} catch (SAXException e) {
							e.printStackTrace();
							fail();
						}
					}
				}
			});
			threads.add(th);
		}
		return threads;
	}
}
