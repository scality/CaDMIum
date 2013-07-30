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
package com.scality.sofs.utils.watch.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;

import com.scality.sofs.utils.GeoSyncServer;
import com.scality.sofs.utils.watch.Factory;
import com.scality.sofs.utils.watch.SofsWatchService;

/**
 * Runs the sample server
 * 
 * @author ziad.bizri@ezako.com for Scality
 * @since 1.7
 */
public class RunServer {

	/**
	 * Server on port 8082
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		run(8082);
	}

	/**
	 * @param port
	 */
	public static void run(int port) {

		// TODO manage arguments like port, amount of threads ...

		final SofsWatchService watcher = Factory.createService();

		Server server = GeoSyncServer.createServer(8082,
				Factory.createProcessor(watcher));

		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Add a path to listen:
		Path path1 = Factory.createPath("/foo/bar1");
		Path path2 = Factory.createPath("/foo/bar2");
		Path path3 = Factory.createPath("/foo/bar3");
		Path path4 = Factory.createPath("/foo/bar4");
		WatchKey key1, key2, key3, key4;
		try {
			key1 = path1.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			key2 = path2.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE);
			key3 = path3.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE);
			key4 = path4
					.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
			System.out.println(key1);
			System.out.println(key2);
			System.out.println(key3);
			System.out.println(key4);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						WatchKey key = watcher.poll(2, TimeUnit.SECONDS);
						List<WatchEvent<?>> events = key.pollEvents();
						for (WatchEvent<?> event : events) {
							System.out.println(event.kind());
							System.out.println(event.context());
						}
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
