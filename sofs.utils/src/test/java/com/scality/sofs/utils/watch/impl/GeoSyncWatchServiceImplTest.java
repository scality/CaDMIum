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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.junit.Test;

import com.scality.sofs.utils.watch.Factory;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 */
public class GeoSyncWatchServiceImplTest {

	@Test
	public void testWatchKeyCreation() throws IOException {
		Path path1 = Factory.createPath("/foo/bar");
		Path path11 = Factory.createPath("/foo/..//foo/bar/");
		Path path2 = Factory.createPath("/foo/baz");

		final WatchService watcher = new WatchServiceImpl();

		WatchKey key = path1.register(watcher, ENTRY_CREATE);

		WatchKey key2 = path11.register(watcher, ENTRY_MODIFY);
		
		WatchKey key3 = path1.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);

		WatchKey key4 = path2.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);

		// One path (not reference, real path) only get 1 WatchKey, no
		// matter Events type
		assertSame(key, key2);
		assertSame(key, key3);
		assertSame(key2, key3);
		// Different path, different key
		assertNotSame(key, key4);
	}

}
