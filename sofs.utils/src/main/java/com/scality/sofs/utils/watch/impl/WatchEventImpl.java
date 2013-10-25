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

import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.watch.SofsWatchEvent;
import com.scality.sofs.utils.watch.impl.events.TimestampEvent;

/**
 * This is a watchEvent implementation
 * 
 * This class is intended for internal use only
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 * @since 1.7
 * 
 */
class WatchEventImpl implements SofsWatchEvent, TimestampEvent {

	/** the event kind */
	Kind<Path> kind;
	/** relative path of the event to the effective Path it is registered to */
	Path relativePath;
	/** the original sofsevent backing this event */
	SofsEvent originalEvent;

	/**
	 * Constructor
	 * 
	 * @param kind
	 * @param relativePath
	 * @param originalEvent
	 */
	WatchEventImpl(Kind<Path> kind, Path relativePath, SofsEvent originalEvent) {
		this.kind = kind;
		this.relativePath = relativePath;
		this.originalEvent = originalEvent;
	}

	@Override
	public java.nio.file.WatchEvent.Kind<Path> kind() {
		return kind;
	}

	@Override
	public int count() {
		return 1;
	}

	@Override
	public Path context() {
		return relativePath;
	}

	@Override
	public String toString() {
		return "[WatchEvent : context:" + relativePath + ", kind: " + kind
				+ "]";
	}

	@Override
	public SofsEvent getOriginalEvent() {
		return originalEvent;
	}

	@Override
	public double timestamp() {
		return originalEvent.getTimestamp();
	}

}
