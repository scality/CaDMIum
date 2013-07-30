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

import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.scality.sofs.utils.watch.SofsWatchService;
import com.scality.sofs.utils.watch.SofsPath;
import com.scality.sofs.utils.watch.SofsWatchKey;

/**
 * 
 * This class is an implementation class, designed to be used within
 * its package
 * 
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 * 
 */
class WatchKeyImpl implements SofsWatchKey {

	/** Available states for this WatchKey, following java 1.7 {@link WatchKey} javadoc */
	enum State {
		READY, SIGNALLED
	};

	/** The path for this WatchKey */
	SofsPath path;

	/**
	 * A list of available events
	 *  This is a thread safe list, since threads can get it through
	 *  getEventKinds()
	 */
	List<Kind<?>> eventKinds = new CopyOnWriteArrayList<Kind<?>>();

	/** Events on this key */
	volatile List<WatchEvent<?>> eventsList = new ArrayList<WatchEvent<?>>();
	/** valid, following java 1.7 watchkey */
	boolean valid = true;
	/** cancelled, following java 1.7 watchkey */
	boolean cancelled = false;
	/** the watchservice this key is registered to */
	SofsWatchService watchService;

	/** the current state of this key, following java 1.7 watchkey */
	volatile State state = State.READY;

	/**
	 * Constructor
	 * @param path
	 * @param watchService
	 * @param kinds
	 */
	WatchKeyImpl(SofsPath path, WatchService watchService, Kind<?>... kinds) {
		this.path = path;
		for (Kind<?> kind : kinds) {
			eventKinds.add(kind);
		}
		// Might throw a ClassCastException, which is actually what we expect in
		// that
		// case, specific enough for user
		this.watchService = (SofsWatchService) watchService;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public List<WatchEvent<?>> pollEvents() {
		// This must be synchronized to avoid race condition where 2 threads get
		// the same eventsList to process.
		// Maybe the compiler does optimize the following code
		synchronized (this) {
			List<WatchEvent<?>> tempList = eventsList;
			eventsList = new ArrayList<WatchEvent<?>>();
			return tempList;
		}
	}

	@Override
	public boolean reset() {
		synchronized (this) {
			if (eventsList.size() != 0) {
				state = State.SIGNALLED;
				// Requeue this to the watchService
				try {
					((Requeuable) watchService).requeue(this);
				} catch (InterruptedException e) {
					new RuntimeException(
							"Unable to requeue key to watchService", e);
				}
			} else {
				state = State.READY;
			}
			// Check after requeuing because we want to make sure all events
			// have been processed
			if (!isValid())
				return false;
			return true;
		}
	}

	@Override
	public void cancel() {
		valid = false;
		cancelled = true;
		// Cancel this key on the watchService
		try {
			watchService.unregister(this);
		} catch (InterruptedException e) {
			throw new RuntimeException(
					"Exception while unregistering a WatchKey", e);
		}
	}

	@Override
	public Watchable watchable() {
		return path;
	}

	/**
	 * Hook to allow modification of "valid" information
	 * @param valid
	 */
	void setValid(boolean valid) {
		this.valid = valid;
	}

	/*
	 * Making this method synchronized will superslow it Testing did not show
	 * any lost event, since all known caller handle safety by themself.
	 * 
	 * @param event
	 */
	@Override
	public void addEvent(WatchEvent<?> event) {
		eventsList.add(event);
		state = State.SIGNALLED;
	}

	@Override
	public List<Kind<?>> getEventKinds() {
		return eventKinds;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[WatchKeyImpl: ").append(path.getPath()).append(" [");
		for (Kind<?> kind : eventKinds) {
			sb.append(kind).append(" ");
		}
		sb.append("] ]");
		return sb.toString();
	}

	@Override
	public boolean isReady() {
		return State.READY.equals(state);
	}
	
}
