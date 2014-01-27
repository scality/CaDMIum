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
package com.scality.sofs.utils.watch.impl.events;

import java.nio.file.WatchEvent;
import java.util.Comparator;

/**
 * A comparator for TimestampEvent, comparing by timestamp.
 * 
 * If non-timestamp events are provided, this comparator will still be able to
 * send a constant result avoiding to break its contract. Nevertheless, this
 * result would be functionally useless and meaningless.
 * 
 * @see TimestampEvent
 * @author julien.muller@ezako.com
 * 
 */
public class TimestampEventComparator implements Comparator<WatchEvent<?>> {

	@Override
	public int compare(WatchEvent<?> event1, WatchEvent<?> event2) {

		// Make sure the event types are ok
		if (!(event1 instanceof TimestampEvent)
				&& !(event2 instanceof TimestampEvent))
			return 0;

		// Compare must be consistant
		if (!(event1 instanceof TimestampEvent))
			return -1;

		if (!(event2 instanceof TimestampEvent))
			return 1;
		// Finally a simple timestamp compare ...
		return Double.compare(((TimestampEvent) event1).timestamp(),
				((TimestampEvent) event2).timestamp());
	}

}
