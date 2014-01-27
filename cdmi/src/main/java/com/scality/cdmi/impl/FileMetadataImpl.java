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
package com.scality.cdmi.impl;

import com.scality.cdmi.api.FileMetadata;
import com.scality.cdmi.impl.metadata.CdmiMetadata;

/**
 * Implementation for the {@link FileMetadata} interface.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class FileMetadataImpl implements FileMetadata {
	/**
	 * The key used in the CDMI repository.
	 */
	private final String key;
	/**
	 * The length of the data object, or -1 if it is a container.
	 */
	private final long length;
	/**
	 * Last modification time, in seconds since Epoch.
	 */
	private final long lastModified;
	/**
	 * Last access time, in seconds since Epoch.
	 */
	private final long lastAccessed;
	/**
	 * True if the underlying resource is a container.
	 */
	private final boolean isContainer;
	/**
	 * String representation of the CDMI metadata.
	 */
	private final String cdmiMetadata;

	/**
	 * Constructor.
	 * 
	 * @param key
	 *            the key of the resource in the CDMI repository
	 * @param length
	 *            the length of the data object, or -1 for containers
	 * @param lastmodified
	 *            the time in seconds since Epoch since the last modification
	 * @param lastAccessed
	 *            the time in seconds since Epoch since the last access
	 * @param isdContainer
	 *            true if the resource is a container
	 */
	public FileMetadataImpl(String key, long length, long lastmodified,
			long lastAccessed, boolean isdContainer, String cdmiMetadata) {
		this.key = key;
		this.length = length;
		this.lastModified = lastmodified;
		this.lastAccessed = lastAccessed;
		this.isContainer = isdContainer;
		this.cdmiMetadata = cdmiMetadata;
	}

	/**
	 * Constructor. Extracts the relevant information from a
	 * {@link CdmiMetadata} object.
	 * 
	 * @param meta
	 *            a {@link CdmiMetadata} object
	 */
	public FileMetadataImpl(CdmiMetadata meta) {
		this.key = meta.fullPath();
		this.length = meta.getSize();
		this.lastModified = meta.getMtime();
		this.lastAccessed = meta.getAtime();
		this.isContainer = meta.isContainer();
		this.cdmiMetadata = meta.getMetadata();
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public long getLength() {
		return length;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public long getLastAccessed() {
		return lastAccessed;
	}

	@Override
	public boolean isContainer() {
		return isContainer;
	}

	@Override
	public String getCdmiMetadata() {
		return cdmiMetadata;
	}
}
