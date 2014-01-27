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
package com.scality.sofs.utils.legacy.example;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;

import com.scality.sofs.utils.GeoSyncServer;
import com.scality.sofs.utils.events.SofsEvent;
import com.scality.sofs.utils.legacy.LegacyProcessor;
import com.scality.sofs.utils.legacy.LegacyWatcher;

/**
 * 
 * This server example is very simple and demonstrate
 * how to use the legacy implementation.
 * 
 * In this version, there is only one consumer who 
 * sends the events to System.out.
 * 
 * @author julien.muller@ezako.com for Scality
 *
 */
public class RunServer {

	/**
	 * Run the sample program. 
	 * 
	 * Runs a server on port 8082 and wait for Sofs events
	 * 
	 * @param args no specific args
	 */
	public static void main(String[] args) {
		run(8082);
	}
	
	/**
	 * @param port
	 */
	public static void run(int port) {
		
		final LegacyWatcher watcher = new LegacyWatcher();
		Server server = GeoSyncServer.createServer(port, new LegacyProcessor(watcher));

		System.out.println("Running server on port " + port + "...");
		
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						SofsEvent nextEvent = watcher.poll(2, TimeUnit.SECONDS);
						
						// We are only sysouting the event, implementations can
						// use the nextEvent object to do much more
						if (nextEvent != null) System.out.println(nextEvent);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		try {
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
