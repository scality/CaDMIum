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

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.scality.sofs.utils.GeoSyncPayLoadProcessingException;
import com.scality.sofs.utils.events.AbstractEventPayLoadProcessor;
import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.watch.OverflowException;
import com.scality.sofs.utils.watch.SofsWatchService;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 * 
 */
public class WatchPayLoadProcessor extends AbstractEventPayLoadProcessor {

	/** the WatchService this processor will addEvents to */
	protected SofsWatchService watchService;

	/**
	 * Constructor
	 * 
	 * @param watchService
	 */
	public WatchPayLoadProcessor(SofsWatchService watchService) {
		this.watchService = watchService;
	}

	@Override
	public void processEvent(SofsEvent event)
			throws GeoSyncPayLoadProcessingException {

		List<Kind<Path>> eventsMap = EventsMapper.mapEvents(event
				.getOperationType());
		try {
			if (eventsMap.size() == 0 || eventsMap.size() > 2) {
				// Throw an exception
				throw new GeoSyncPayLoadProcessingException(
						"EventsMapper returned an odd number of events: "
								+ eventsMap.size());
			} else {
				// Most common case, this events maps to 1 event
				watchService.addEvent(event.getPath(), eventsMap.get(0), event);
			}
			if (eventsMap.size() == 2) {
				// this events maps to 2 events
				watchService.addEvent(event.getPath2(), eventsMap.get(1),
						(SofsEvent) (event.clone()));
			}
		} catch (InterruptedException ie) {
			throw new GeoSyncPayLoadProcessingException(
					"InterruptedException occurred while processing event", ie);
		} catch (CloneNotSupportedException e) {
			// Should not arrive here
			throw new GeoSyncPayLoadProcessingException(
					"Unable to clone the sofsevent", e);
		} catch (OverflowException e) {
			// Not able to process this event, because of an overflow
			throw new GeoSyncPayLoadProcessingException(
					"Unable to clone the sofsevent", e,
					HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}

}
