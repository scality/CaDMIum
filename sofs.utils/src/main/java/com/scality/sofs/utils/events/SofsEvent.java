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
package com.scality.sofs.utils.events;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 * A bean representing an event of type GeoSync Sofs
 * 
 * <pre>
 * event_log_version: 1
 * timestamp: time of event
 * ring_key: object key
 * operation_type: one of {@link SofsEventTypes}
 * path: path affected by operation
 * [path2]: optional secondary path separated by a NUL character for RENAMENOD,
 * RENAMEDIR and LINK
 * </pre>
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class SofsEvent {

	/**
	 * @param eventLogVersion
	 * @param timestamp
	 * @param ringKey
	 * @param operationType
	 * @param path
	 * @param path2
	 */
	public SofsEvent(int eventLogVersion, double timestamp, String ringKey,
			SofsEventTypes operationType, String path, String path2) {
		super();
		this.eventLogVersion = eventLogVersion;
		this.timestamp = timestamp;
		this.ringKey = ringKey;
		this.operationType = operationType;
		// Normalization is an extensive algorithm, commons.io
		this.path = FilenameUtils.normalizeNoEndSeparator(path);
		this.path2 = FilenameUtils.normalizeNoEndSeparator(path2);
	}

	/** event_log_version */
	protected int eventLogVersion;
	/** time of event */
	protected double timestamp;
	/** ring_key */
	protected String ringKey;
	/** operation_type */
	protected SofsEventTypes operationType;
	/** path */
	protected String path;
	/** optional secondary path for RENAMENOD, RENAMEDIR and LINK */
	protected String path2;
	/** Is it a duplicate of the event or the actual original event */
	protected boolean duplicate = false;

	/**
	 * @return true if this is a duplicate, false if this is the original event
	 */
	public boolean isDuplicate() {
		return duplicate;
	}

	/**
	 * @return a boolean true if this bean has a path2, false otherwise
	 */
	public boolean hasPath2() {
		switch (operationType) {
		case RENAMENOD:
		case RENAMEDIR:
		case LINK:
			return true;
		default:
			return false;
		}
	}

	/**
	 * @return an integer representing the event log version
	 */
	public int getEventLogVersion() {
		return eventLogVersion;
	}

	/**
	 * @return the timestamp
	 */
	public double getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the ring key
	 */
	public String getRingKey() {
		return ringKey;
	}

	/**
	 * @return operationType
	 */
	public SofsEventTypes getOperationType() {
		return operationType;
	}

	/**
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the second path in case of multi path action (move ...)
	 */
	public String getPath2() {
		return path2;
	}

	@Override
	public String toString() {
		return "SofsEvent [eventLogVersion=" + eventLogVersion + ", timestamp="
				+ timestamp + ", ringKey=" + ringKey + ", operationType="
				+ operationType + ", path=" + path + ", path2=" + path2
				+ ", hasPath2()=" + hasPath2() + ", isDuplicate()="
				+ isDuplicate() + "]";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SofsEvent clone = new SofsEvent(eventLogVersion, timestamp, ringKey,
				operationType, path, path2);
		clone.duplicate = true;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SofsEvent))
			return false;
		SofsEvent other = (SofsEvent) obj;

		if (this.eventLogVersion != other.eventLogVersion)
			return false;
		if (this.timestamp != other.timestamp)
			return false;
		if (other.ringKey == null && this.ringKey != null)
			return false;
		if (!this.ringKey.equals(other.ringKey))
			return false;
		if (!this.ringKey.equals(other.ringKey))
			return false;
		if (this.operationType != null
				&& !this.operationType.equals(other.operationType))
			return false;
		if (this.path == null && other.path != null)
			return false;
		if (other.path == null && this.path != null)
			return false;
		if (this.path != null && !this.path.equals(other.path))
			return false;
		if (this.path2 == null && other.path2 != null)
			return false;
		if (other.path2 == null && this.path2 != null)
			return false;
		if (this.path2 != null && !this.path2.equals(other.path2))
			return false;
		if (this.duplicate != other.duplicate)
			return false;
		return true;
	}

}
