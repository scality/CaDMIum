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
package com.scality.sofs.utils.watch.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.scality.sofs.utils.GeoSyncPayLoadProcessingException;
import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.events.SofsEventTypes;
import com.scality.sofs.utils.watch.SofsWatchService;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 */
public class WatchPayLoadProcessorTest {

	public static class MockWatchService implements SofsWatchService {

		List<Event> events = new ArrayList<Event>();
		
		@Override
		public void close() throws IOException {
		}

		@Override
		public WatchKey poll() {
			return null;
		}

		@Override
		public WatchKey poll(long timeout, TimeUnit unit)
				throws InterruptedException {
			return null;
		}

		@Override
		public WatchKey take() throws InterruptedException {
			return null;
		}

		@Override
		public void unregister(WatchKey key) throws InterruptedException {
		}

		@Override
		public void addEvent(String path, Kind<Path> kind,
				SofsEvent originalEvent) throws InterruptedException {
			events.add(new Event(path, kind, originalEvent));
		}
		
		public List<Event> getEventsForTest() {
			return events;
		}
		
		public static class Event {
			public String path;
			public Event(String path, Kind<Path> kind, SofsEvent originalEvent) {
				super();
				this.path = path;
				this.kind = kind;
				this.originalEvent = originalEvent;
			}
			public Kind<Path> kind;
			public SofsEvent originalEvent;
		}

		@Override
		public WatchKey register(com.scality.sofs.utils.watch.SofsPath path,
				Kind<?>... events) throws InterruptedException {
			return null;
		}
	}
	
	@Test
	public void test() {
		MockWatchService service = new MockWatchService();
		WatchPayLoadProcessor processor = new WatchPayLoadProcessor(service);
		
		SofsEvent event = new SofsEvent(1, 231.321, "RINGKEY123", SofsEventTypes.CONTENT_MODIFIED, "/foo/bar/baz", null);
		
		try {
			processor.processEvent(event);
		} catch (GeoSyncPayLoadProcessingException e) {
			e.printStackTrace();
			fail("Should not get an exception");
		}
		
		assertEquals(event, service.getEventsForTest().get(0).originalEvent);
		assertEquals("/foo/bar/baz", service.getEventsForTest().get(0).path);
		assertEquals(StandardWatchEventKinds.ENTRY_MODIFY, service.getEventsForTest().get(0).kind);
		
	}
	
}
