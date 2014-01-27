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
package com.scality.sofs.utils.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.scality.sofs.utils.GeoSyncPayLoadProcessingException;
import com.scality.sofs.utils.events.AbstractEventPayLoadProcessor;
import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.events.SofsEventTypes;

/**
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class AbstractPayloadProcessorTest {

	/**
	 * This class is just a mock and should not be used out of its context it is
	 * definitly not thread safe and not really usable in real context
	 */
	public static class MockProcessor extends AbstractEventPayLoadProcessor {
		List<SofsEvent> allEvents = new ArrayList<SofsEvent>();

		@Override
		public void processEvent(SofsEvent event) {
			allEvents.add(event);
		}

		public List<SofsEvent> getEvents() {
			return allEvents;
		}
	}

	@Test
	public void basicSuccess() throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKDIR:::/foo/include";
		MockProcessor processor = new MockProcessor();
		processor.process(content);

		assertEquals(1, processor.getEvents().size());

		SofsEvent result = processor.getEvents().get(0);
		assertEquals(1, result.getEventLogVersion());
		assertEquals(1364220640.307028, result.getTimestamp(), 0.001);
		assertEquals("889410000CB25F0CC9CF43000000010200000040",
				result.getRingKey());
		assertEquals(SofsEventTypes.MKDIR, result.getOperationType());
		assertEquals("/foo/include", result.getPath());
		assertNull(result.getPath2());
		assertFalse(result.hasPath2());
	}

	@Test
	public void withPath2Success() throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:RENAMEDIR:::/foo/include\0/foo/include2";
		MockProcessor processor = new MockProcessor();
		processor.process(content);

		assertEquals(1, processor.getEvents().size());

		SofsEvent result = processor.getEvents().get(0);
		assertEquals(1, result.getEventLogVersion());
		assertEquals(1364220640.307028, result.getTimestamp(), 0);
		assertEquals("889410000CB25F0CC9CF43000000010200000040",
				result.getRingKey());
		assertEquals(SofsEventTypes.RENAMEDIR, result.getOperationType());
		assertEquals("/foo/include", result.getPath());
		assertNotNull(result.getPath2());
		assertTrue(result.hasPath2());
		assertEquals("/foo/include2", result.getPath2());
	}

	@Test
	public void multipleSuccess() throws GeoSyncPayLoadProcessingException {

		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKDIR:::/foo/include\n"
				+ "1:1364220648.847604:32D745056A8E9D6943CBF0000000010200000020:MKNOD:::/foo/include/paths.h\n"
				+ "1:1364220648.848787:32D745056A8E9D6943CBF0000000010200000020:ATTR_MODIFIED:::/foo/include/paths.h\n"
				+ "1:1364220648.881096:0F754106F388BFABF4B750000000010200000020:MKNOD:::/foo/include/unctrl.h\n"
				+ "1:1364220648.860763:32D745056A8E9D6943CBF0000000010200000020:CONTENT_MODIFIED:::/foo/include/paths.h";

		MockProcessor processor = new MockProcessor();
		processor.process(content);

		assertEquals(5, processor.getEvents().size());

		SofsEvent result = processor.getEvents().get(0);
		assertEquals(1, result.getEventLogVersion());
		assertEquals(1364220640.307028, result.getTimestamp(), 0);
		assertEquals("889410000CB25F0CC9CF43000000010200000040",
				result.getRingKey());
		assertEquals(SofsEventTypes.MKDIR, result.getOperationType());
		assertEquals("/foo/include", result.getPath());
		assertNull(result.getPath2());
		assertFalse(result.hasPath2());

		result = processor.getEvents().get(2);
		assertEquals(1, result.getEventLogVersion());
		assertEquals(1364220648.848787, result.getTimestamp(), 0);
		assertEquals("32D745056A8E9D6943CBF0000000010200000020",
				result.getRingKey());
		assertEquals(SofsEventTypes.ATTR_MODIFIED, result.getOperationType());
		assertEquals("/foo/include/paths.h", result.getPath());
		assertNull(result.getPath2());
		assertFalse(result.hasPath2());

	}

	@Test(expected = GeoSyncPayLoadProcessingException.class)
	public void simpleFailure() throws GeoSyncPayLoadProcessingException {
		String content = "A:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:RENAMEDIR:::/foo/include\0/foo/include2";
		MockProcessor processor = new MockProcessor();
		try {
			processor.process(content);
			fail("Should get an exception before this");
		} catch (GeoSyncPayLoadProcessingException e) {
			assertTrue(e.getCause() instanceof NumberFormatException);
			throw e;
		}
	}

	@Test(expected = GeoSyncPayLoadProcessingException.class)
	public void parsingTSFailure() throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640;307028:889410000CB25F0CC9CF43000000010200000040:RENAMEDIR:::/foo/include\0/foo/include2";
		MockProcessor processor = new MockProcessor();
		try {
			processor.process(content);
			fail("Should get an exception before this");
		} catch (GeoSyncPayLoadProcessingException e) {
			assertTrue(e.getCause() instanceof NumberFormatException);
			throw e;
		}
	}

	@Test(expected = GeoSyncPayLoadProcessingException.class)
	public void parsingNbColumnFailure()
			throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKDIR:::/foo/include:FAILCOLUMN";
		MockProcessor processor = new MockProcessor();
		try {
			processor.process(content);
			fail("Should get an exception before this");
		} catch (GeoSyncPayLoadProcessingException e) {
			assertTrue(e.getMessage().contains("Unexpected format"));
			throw e;
		}
	}

	@Test(expected = GeoSyncPayLoadProcessingException.class)
	public void parsingNbColumn2Failure()
			throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKDIR::/foo/include";
		MockProcessor processor = new MockProcessor();
		try {
			processor.process(content);
			fail("Should get an exception before this");
		} catch (GeoSyncPayLoadProcessingException e) {
			assertTrue(e.getMessage().contains("Unexpected format"));
			throw e;
		}
	}

	@Test(expected = GeoSyncPayLoadProcessingException.class)
	public void parsingEventTypeFailure()
			throws GeoSyncPayLoadProcessingException {
		String content = "1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKODIR:::/foo/include";
		MockProcessor processor = new MockProcessor();
		try {
			processor.process(content);
			fail("Should get an exception before this");
		} catch (GeoSyncPayLoadProcessingException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException);
			throw e;
		}
	}

}
