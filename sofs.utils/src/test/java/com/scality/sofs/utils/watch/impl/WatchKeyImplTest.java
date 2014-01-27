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

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import org.junit.Assert;
import org.junit.Test;

import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.events.SofsEventTypes;
import com.scality.sofs.utils.watch.Factory;
import com.scality.sofs.utils.watch.OverflowException;
import com.scality.sofs.utils.watch.SofsPath;
import com.scality.sofs.utils.watch.SofsWatchEventModifier;
import com.scality.sofs.utils.watch.impl.events.OverflowEvent;
import com.scality.sofs.utils.watch.impl.events.TimestampEvent;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 */
public class WatchKeyImplTest {

	@Test
	public void simple() {

		@SuppressWarnings("unchecked")
		WatchEvent.Kind<Path> kinds[] = new WatchEvent.Kind[2];
		kinds[0] = StandardWatchEventKinds.ENTRY_CREATE;
		kinds[1] = StandardWatchEventKinds.ENTRY_MODIFY;

		WatchKeyImpl key = new WatchKeyImpl(
				(SofsPath) Factory.createPath("/foo/bar/baz"), null, kinds);
		assertEquals(Factory.createPath("/foo/bar/baz"), key.watchable());

		assertEquals(StandardWatchEventKinds.ENTRY_CREATE,
				key.eventKinds.get(0));

		assertEquals(StandardWatchEventKinds.ENTRY_MODIFY,
				key.eventKinds.get(1));
	}

	@Test
	public void overflowBasic() {
		// No overflow under max size
		WatchKey key = createOverflowEvents(WatchKeyImpl.EVENT_LIST_MAX_SIZE - 1);

		for (WatchEvent<?> event : key.pollEvents()) {
			if (event.kind().equals(StandardWatchEventKinds.OVERFLOW))
				Assert.fail("Overflow under the list size");
		}
	}

	@Test
	public void overflowBasic2() {
		// Overflow at max size
		WatchKey key = createOverflowEvents(WatchKeyImpl.EVENT_LIST_MAX_SIZE);

		for (WatchEvent<?> event : key.pollEvents()) {
			if (event.kind().equals(StandardWatchEventKinds.OVERFLOW))
				Assert.fail("Overflow under the list size");
		}
	}

	@Test
	public void overflowBasic3() {
		// Overflow after max size
		WatchKey key = createOverflowEvents(WatchKeyImpl.EVENT_LIST_MAX_SIZE + 1);

		int count = 0;
		for (WatchEvent<?> event : key.pollEvents()) {
			if (event.kind().equals(StandardWatchEventKinds.OVERFLOW))
				count++;
		}

		Assert.assertTrue(count == 1);
	}

	@Test
	public void overflowBasic4() {
		// Overflow after max size
		WatchKey key = createOverflowEvents(WatchKeyImpl.EVENT_LIST_MAX_SIZE + 100);

		int count = 0, countOverflow = 0;
		for (WatchEvent<?> event : key.pollEvents()) {
			count++;
			if (event.kind().equals(StandardWatchEventKinds.OVERFLOW))
				countOverflow++;
		}
		// Max size + overflow event
		Assert.assertTrue(count == WatchKeyImpl.EVENT_LIST_MAX_SIZE + 1);
		Assert.assertTrue(countOverflow == 1);
	}

	@Test
	public void overflowExceptions() {
		Assert.assertTrue(createOverflowException(WatchKeyImpl.EVENT_LIST_MAX_SIZE) == -1);
		Assert.assertTrue(createOverflowException(WatchKeyImpl.EVENT_LIST_MAX_SIZE + 1) == WatchKeyImpl.EVENT_LIST_MAX_SIZE);
		// Not really usefull, but make the 100% coverage more clear
		Assert.assertTrue(createOverflowException(WatchKeyImpl.EVENT_LIST_MAX_SIZE + 10) == WatchKeyImpl.EVENT_LIST_MAX_SIZE);
	}

	@Test
	public void eventsOrderedByTimestamp() {
		// Make sure the pollEvents order is correct
		WatchKeyImpl key = new WatchKeyImpl(
				(SofsPath) Factory.createPath("/foo/bar/baz"), null,
				new WatchEvent.Kind[0]);

		try {
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 2d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 4d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 1d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 3d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			double val = 1;
			for (WatchEvent<?> event : key.pollEvents()) {
				if (((WatchEventImpl) event).timestamp() != val++)
					Assert.fail("Unexpected events order, might have an issue with TimestampEventComparator");
			}
		} catch (OverflowException e) {
			Assert.fail("Unexpected overflow");
		}
	}

	@Test
	public void eventsOrderedByTimestamp2() {
		// Make sure the pollEvents order is correct
		WatchKeyImpl key = new WatchKeyImpl(
				(SofsPath) Factory.createPath("/foo/bar/baz"), null,
				new WatchEvent.Kind[0]);

		try {
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 2d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 2d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 1d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new WatchEventImpl(
					StandardWatchEventKinds.ENTRY_CREATE, null, new SofsEvent(
							1, 2d, "ringKey", SofsEventTypes.MKDIR,
							"/test/test", "")));
			key.addEvent(new OverflowEvent(2d));
			key.addEvent(new OverflowEvent(1d));
			key.addEvent(new OverflowEvent(3d));
			key.addEvent(new WatchEventImpl(null, null, new SofsEvent(1, 2d,
					"ringKey", SofsEventTypes.MKDIR, "/test/test", "")));

			for (WatchEvent<?> event : key.pollEvents()) {
				System.out.println(((TimestampEvent) event).timestamp());
			}
		} catch (OverflowException e) {
			Assert.fail("Unexpected overflow");
		}
	}

	private int createOverflowException(int size) {
		WatchKeyImpl key = new WatchKeyImpl(
				(SofsPath) Factory.createPath("/foo/bar/baz"), null,
				new WatchEvent.Kind[0],
				SofsWatchEventModifier.CLOSE_CONN_ON_OVERFLOW);

		// Check for overflow before max size
		for (int i = 0; i < size; i++) {
			try {
				key.addEvent(new WatchEventImpl(
						StandardWatchEventKinds.ENTRY_CREATE, null,
						new SofsEvent(1, 1.1 + i, "ringKey",
								SofsEventTypes.MKDIR, "/test/test", "")));
			} catch (OverflowException e) {
				return i;
			}
		}
		// No exception
		return -1;
	}

	private WatchKey createOverflowEvents(int size) {
		WatchKeyImpl key = new WatchKeyImpl(
				(SofsPath) Factory.createPath("/foo/bar/baz"), null,
				new WatchEvent.Kind[0]);

		// Check for overflow before max size
		for (int i = 0; i < size; i++) {
			// key.addEvent(new
			// WatchEventImpl(StandardWatchEventKinds.ENTRY_CREATE,
			// Factory.createPath("/" + i), (SofsEvent)null);
			try {
				key.addEvent(new WatchEventImpl(
						StandardWatchEventKinds.ENTRY_CREATE, null,
						new SofsEvent(1, 1.1 + i, "ringKey",
								SofsEventTypes.MKDIR, "/test/test", "")));

			} catch (OverflowException e) {
				e.printStackTrace();
				Assert.fail("Overflow exception at " + i);
			}
		}
		return key;
	}

}
