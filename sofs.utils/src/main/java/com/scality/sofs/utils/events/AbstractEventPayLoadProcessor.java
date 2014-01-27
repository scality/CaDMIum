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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.scality.sofs.utils.GeoSyncPayLoadProcessingException;
import com.scality.sofs.utils.GeoSyncPayLoadProcessor;

/**
 * An abstract implementation of GeoSyncPayLoadProcessor
 * 
 * This class provides a mapping from event format to an object
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public abstract class AbstractEventPayLoadProcessor implements
		GeoSyncPayLoadProcessor {

	/**
	 * Provides a hook to handle the event at an object format
	 * 
	 * @param event
	 * @throws GeoSyncPayLoadProcessingException
	 */
	public abstract void processEvent(SofsEvent event)
			throws GeoSyncPayLoadProcessingException;

	/**
	 * Processes a GEOSYNC log at the following format:
	 * 
	 * event_log_version:timestamp:ring_key:operation_type:::path[\0path2]
	 * 
	 * <ul>
	 * <li>event_log_version = 1</li>
	 * <li>timestamp = time of event</li>
	 * <li>ring_key = object key</li>
	 * <li>operation_type can be:
	 * <ul>
	 * <li>MKNOD -> file creation</li>
	 * <li>MKDIR -> directory creation</li>
	 * <li>UNLINK -> file removal</li>
	 * <li>RMDIR -> dir removal</li>
	 * <li>RENAMENOD -> rename file</li>
	 * <li>RENAMEDIR -> rename dir</li>
	 * <li>LINK -> hardlink</li>
	 * <li>SYMLINK -> symlink</li>
	 * <li>ATTR_MODIFIED -> attribute modified</li>
	 * <li>CONTENT_MODIFIED -> content modified</li>
	 * </ul>
	 * </li>
	 * <li>path = path affected by operation [\0path2] = optional secondary path
	 * separated by a NUL character for RENAMENOD, RENAMEDIR and LINK</li>
	 * 
	 * <pre>
	 * Example of event log: 
	 * ---------------------
	 * <code>
	 * 	 	1:1364220640.307028:889410000CB25F0CC9CF43000000010200000040:MKDIR:::/foo/include
	 * 	 	1:1364220648.847604:32D745056A8E9D6943CBF0000000010200000020:MKNOD:::/foo/include/paths.h
	 * 	 	1:1364220648.848787:32D745056A8E9D6943CBF0000000010200000020:ATTR_MODIFIED:::/foo/include/paths.h
	 * 	 	1:1364220648.881096:0F754106F388BFABF4B750000000010200000020:MKNOD:::/foo/include/unctrl.h
	 * 	 	1:1364220648.860763:32D745056A8E9D6943CBF0000000010200000020:CONTENT_MODIFIED:::/foo/include/paths.h
	 * 	   </code>
	 * </pre>
	 */
	@Override
	public void process(String payLoad)
			throws GeoSyncPayLoadProcessingException {

		// process payload line by line
		BufferedReader buff = new BufferedReader(new StringReader(payLoad));
		String line = null;
		try {
			while ((line = buff.readLine()) != null) {
				String[] record = line.split(":");
				if (record.length != 7) {
					throw new GeoSyncPayLoadProcessingException(
							"Unexpected format geosync event: " + line);
				}
				String[] allPaths = record[6].split(Character.toString('\0'));
				String path = allPaths[0];
				String path2 = (allPaths.length > 1) ? allPaths[1] : null;

				SofsEvent event = new SofsEvent(Integer.parseInt(record[0]),
						Double.parseDouble(record[1]), record[2],
						SofsEventTypes.valueOf(record[3]), path, path2);

				processEvent(event);
			}
		} catch (NumberFormatException nfe) {
			throw new GeoSyncPayLoadProcessingException(
					"Error while processing a record, NumberFormatException: "
							+ nfe.getMessage(), nfe);
		} catch (IllegalArgumentException iae) {
			throw new GeoSyncPayLoadProcessingException(
					"Error while processing a record event name, IllegalArgumentException: "
							+ iae.getMessage(), iae);
		} catch (IOException ioe) {
			throw new GeoSyncPayLoadProcessingException(
					"Error while processing a record, IOException: "
							+ ioe.getMessage(), ioe);
		} finally {
			if (buff != null) {
				try {
					buff.close();
				} catch (IOException ioe) {
					throw new GeoSyncPayLoadProcessingException(
							"Error while closing a record, IOException: "
									+ ioe.getMessage(), ioe);
				}
			}
		}
	}
}
