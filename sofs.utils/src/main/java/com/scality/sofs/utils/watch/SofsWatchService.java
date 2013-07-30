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

import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.scality.sofs.utils.events.SofsEvent;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 *
 */
public interface SofsWatchService extends WatchService {

	/**
	 * Registers a path to this watchService
	 * If already registered, 
	 * Check if the events are same and add if necessary
	 * Otherwise, this has no effect
	 * 
	 * @param path
	 * @param events
	 * @return the watchkey associated to this path
	 * @throws InterruptedException
	 */
	public WatchKey register(SofsPath path, Kind<?>... events) throws InterruptedException;
	
	/**
	 * Unregister a key from this service.
	 * If not registered, this has no effect
	 * @param key
	 * @throws InterruptedException
	 */
	public void unregister(WatchKey key) throws InterruptedException;

	/**
	 * When an event occurs, it is added to the WatchService
	 * with this method
	 * @param path path to be used
	 * @param kind the kind of the event
	 */
	public void addEvent(String path, Kind<java.nio.file.Path> kind, SofsEvent originalEvent) throws InterruptedException;
	
}
