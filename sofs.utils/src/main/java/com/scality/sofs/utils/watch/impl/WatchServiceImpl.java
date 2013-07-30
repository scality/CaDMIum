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

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.watch.Factory;
import com.scality.sofs.utils.watch.SofsWatchService;
import com.scality.sofs.utils.watch.SofsPath;
import com.scality.sofs.utils.watch.SofsWatchKey;

/**
 * 
 * An implementation of WatchService.
 * This class is intended for internal use only
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 * @since 1.7
 * 
 */
public class WatchServiceImpl implements SofsWatchService, Requeuable {

	/** the blockingqueue of watchkeys */
	protected BlockingQueue<WatchKey> queue = new LinkedBlockingDeque<WatchKey>();

	/** Map of currently watched keys */
	protected Map<String, SofsWatchKey> watching = new ConcurrentHashMap<String, SofsWatchKey>();

	@Override
	public void close() throws IOException {

		// Invalidate the watchkeys as described in WatchKey javadoc
		for (Entry<String, SofsWatchKey> watchKeysEntry : watching.entrySet()) {
			((WatchKeyImpl) watchKeysEntry.getValue()).setValid(false);
		}

	}

	@Override
	public WatchKey poll() {
		return queue.poll();
	}

	@Override
	public WatchKey poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		return queue.poll(timeout, unit);
	}

	@Override
	public WatchKey take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public synchronized WatchKey register(SofsPath path, Kind<?>... events)
			throws InterruptedException {
		String p = path.getPath();
		SofsWatchKey key = watching.get(p);
		if (key == null) {
			key = new WatchKeyImpl(path, this, events);
			watching.put(p, key);
		} else {
			// Make sure it is registered for the same events
			for (Kind<?> event : events) {
				if (!key.getEventKinds().contains(event)) {
					// if not, add the event
					key.getEventKinds().add(event);
				}
			}
		}
		return key;
	}

	@Override
	public synchronized void unregister(WatchKey key)
			throws InterruptedException {
		// If the key is in the queue we will it there
		SofsPath path = (SofsPath) key.watchable();
		String p = path.getPath();
		if (watching.containsKey(p)) {
			watching.remove(p);
		}
	}

	@Override
	public void addEvent(String path, Kind<java.nio.file.Path> kind,
			SofsEvent originalEvent) throws InterruptedException {

		// first we need to construct a parent path
		SofsPath initialPath = (SofsPath)Factory.createPath(path);

		SofsPath watchPath = (SofsPath) initialPath.getParent();

		// If we are watching this event
		if (watching.containsKey(watchPath.getPath())) {
			// Add this event to queue
			SofsWatchKey key = (SofsWatchKey) watching.get(watchPath.getPath());
			synchronized (key) {
				if (key.getEventKinds().contains(kind)) {
					// create a relative path
					SofsPath relativePath = (SofsPath) watchPath
							.relativize(initialPath);

					// This kind is watch by this key, add it
					WatchEvent<java.nio.file.Path> watchEvent = new WatchEventImpl(
							kind, relativePath, originalEvent);

					boolean needToAddToQueue = key.isReady();
					key.addEvent(watchEvent);

					// If the key is ready, it is added to the queue
					if (needToAddToQueue)
						queue.put(key);
					// else it means it is already in the queue or being
					// processed, and will be returned to the queue after
					// reset
				}
			}

		}
		// else we are not watching this path so it has to be ignored
	}

	@Override
	public void requeue(WatchKey key) throws InterruptedException {
		// Make sure this key is watched is not adviced, because it might be
		// A removed key, there is a risk of queuing an unknown key. Better at
		// least get a classcast
		queue.put((SofsWatchKey) key);
	}

}
