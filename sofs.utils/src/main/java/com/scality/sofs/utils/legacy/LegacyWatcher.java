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
package com.scality.sofs.utils.legacy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.scality.sofs.utils.events.SofsEvent;

/**
 * 
 * This class is a custom implementation working with legacy versions of Java
 * (before java 7)
 * 
 * It can be either used as an example for specific implementations or as is.
 * 
 * This Watcher specific implementation is designed to receive Geosync events
 * from multiple sources and have events consumed by multiple clients.
 * 
 * It answers to a contract similar to java 7 Watcher but highly simplified to
 * maintain only the core functionality of filesystem events consumption
 * 
 * This class follows a simple producer / consumer pattern - producers use
 * <code>addEvent</code> to queue an event. - consumers can choose multiple ways
 * to consume, polling or taking.
 * 
 * If the need is exactly the same as java 1.7 watcher, consider doing a custom
 * implementation of LegacyWatcher to introduce an event filtering based on the
 * event's path and type.
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class LegacyWatcher {
	/**
	 * The actual queue for storing the events.
	 */
	protected BlockingQueue<SofsEvent> queue = new LinkedBlockingDeque<SofsEvent>();

	/**
	 * 
	 * Add an event to the queue This method is designed for providers, as this
	 * class is following a producer / consumer pattern.
	 * 
	 * @param event
	 * @throws InterruptedException
	 */
	public void addEvent(SofsEvent event) throws InterruptedException {
		queue.put(event);
	}

	/**
	 * Retrieves and removes the next SofsEvent, or {@code null} if none are
	 * present.
	 * 
	 * @return the next SofsEvent, or {@code null}
	 * 
	 * @throws ClosedWatchServiceException
	 *             if this watch service is closed
	 */
	public SofsEvent poll() {
		return queue.poll();
	}

	/**
	 * Retrieves and removes the next SofsEvent, waiting if necessary up to the
	 * specified wait time if none are yet present.
	 * 
	 * @param timeout
	 *            how to wait before giving up, in units of unit
	 * @param unit
	 *            a {@code TimeUnit} determining how to interpret the timeout
	 *            parameter
	 * 
	 * @return the next {@link SofsEvent}, or {@code null}
	 * 
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public SofsEvent poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		return queue.poll(timeout, unit);
	}

	/**
	 * Retrieves and removes next {@link SofsEvent}, waiting if none are yet
	 * present.
	 * 
	 * @return the next {@link SofsEvent}
	 * 
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public SofsEvent take() throws InterruptedException {
		return queue.take();
	}

}
