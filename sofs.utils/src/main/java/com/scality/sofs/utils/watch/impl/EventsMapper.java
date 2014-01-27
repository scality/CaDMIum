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
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;

import com.scality.sofs.utils.events.SofsEventTypes;

/**
 * A mapping of {@link SofsEventTypes} to {@link StandardWatchEventKinds}.
 * 
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 *
 */
public class EventsMapper {

	/**
	 *	Mapping table:
	 * <pre>
	 *	MKNOD -> ENTRY_CREATE path
	 *	MKDIR -> ENTRY_CREATE path
	 *	UNLINK-> ENTRY_DELETE path
	 *	RMDIR -> ENTRY_DELETE path
	 *	RENAMENOD-> ENTRY_DELETE path, ENTRY_CREATE path2
	 *	RENAMEDIR-> ENTRY_DELETE path, ENTRY_CREATE path2
	 *	LINK     -> ENTRY_CREATE path2
	 *	SYMLINK  -> ENTRY_CREATE path2
	 *	ATTR_MODIFIED -> ENTRY_MODIFY
	 *	CONTENT_MODIFIED -> ENTRY_MODIFIED
	 * </pre>
	 */
	public static List<Kind<Path>> mapEvents(SofsEventTypes inputEvent) {
		List<Kind<Path>> resultEvents = new ArrayList<Kind<Path>>();
		
		switch (inputEvent) {
		
		case MKNOD: //ENTRY_CREATE path
		case MKDIR: //ENTRY_CREATE path
			resultEvents.add(StandardWatchEventKinds.ENTRY_CREATE);
			break;

		case UNLINK: //ENTRY_DELETE path
		case RMDIR: //ENTRY_DELETE path
			resultEvents.add(StandardWatchEventKinds.ENTRY_DELETE);
			break;

		case RENAMENOD: //ENTRY_DELETE path, ENTRY_CREATE path2
		case RENAMEDIR: //ENTRY_DELETE path, ENTRY_CREATE path2
			resultEvents.add(StandardWatchEventKinds.ENTRY_DELETE);
			resultEvents.add(StandardWatchEventKinds.ENTRY_CREATE);
			break;
			
		case LINK: //ENTRY_CREATE path2
		case SYMLINK: //ENTRY_CREATE path2
			resultEvents.add(StandardWatchEventKinds.ENTRY_CREATE);
			break;
			
		case ATTR_MODIFIED: //ENTRY_MODIFY
		case CONTENT_MODIFIED: //ENTRY_MODIFY
			resultEvents.add(StandardWatchEventKinds.ENTRY_MODIFY);
			break;
			
		default:
			throw new RuntimeException("This should not be reachable, compile issue in SofsEvents enum");
		}
		
		return resultEvents;
	}

}
