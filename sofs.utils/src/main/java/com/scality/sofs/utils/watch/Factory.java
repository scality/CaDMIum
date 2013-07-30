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

import java.nio.file.WatchService;

import com.scality.sofs.utils.GeoSyncPayLoadProcessor;
import com.scality.sofs.utils.watch.impl.WatchServiceImpl;
import com.scality.sofs.utils.watch.impl.WatchPayLoadProcessor;

/**
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 * @since 1.7
 *
 */
public class Factory {

	/**
	 * Creates a {@link WatchService} for Sofs
	 * @return the watchService
	 */
	public static SofsWatchService createService() {
		return new WatchServiceImpl();
	}

	/**
	 * Creates a processor able to handle Geosync events
	 * @param watchService
	 * @return the processor
	 */
	public static GeoSyncPayLoadProcessor createProcessor(
			SofsWatchService watchService) {
		return new WatchPayLoadProcessor(watchService);
	}

	/**
	 * Creates a Path of type @link {@link SofsPath}
	 * @param path
	 * @return the newly created path
	 */
	public static java.nio.file.Path createPath(String path) {
		if (path==null) return null;
		return new SofsPath(path);
	}

}
