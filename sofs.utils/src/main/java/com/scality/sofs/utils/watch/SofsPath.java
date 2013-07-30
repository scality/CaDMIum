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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;

/**
 *
 * This is a very limited implementation of java.nio.file.Path
 * providing only mandatory methods for register / unregister
 * to a WatchService
 * 
 * Create a SofsPath object through:
 * <pre><code>
 * SofsPath p = com.scality.sofs.utils.watch.Factory.createPath("/foo/bar");
 * </code><pre>
 *
 * @author julien.muller@ezako.com for Scality
 *
 * @since 1.7
 *
 */
public class SofsPath implements Watchable, java.nio.file.Path {

	private static final String SEPARATOR = "/";
	
	protected String path;
	protected boolean absolute;

	/**
	 * @return the path as a string
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 */
	SofsPath(String path) {
		// Normalization is an extensive algorithm, commons.io
		this.path = FilenameUtils.normalizeNoEndSeparator(path);
		
		if (this.path==null) {
			throw new NullPointerException("The path provided is null after normalization");
		}
		
		if (this.path.startsWith(SEPARATOR)) absolute = true;
		else absolute = false;
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events,
			Modifier... modifiers) throws IOException {
		// TODO Take modifier into account
		// but std javadoc 7 says: This release does not define any <em>standard</em> modifiers
		return register(watcher, events);
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events)
			throws IOException {
		if (!(watcher instanceof SofsWatchService)) 
			throw new UnsupportedOperationException("Only GeoSyncWatchService watcher is supported by this type");
		try {
			return ((SofsWatchService)watcher).register(this, events);
		} catch (InterruptedException e) {
			throw new RuntimeException("InterruptedException while registering the key", e);
		}
	}
	
	@Override
	public FileSystem getFileSystem() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public java.nio.file.Path getRoot() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path getFileName() {
		return new SofsPath(path.substring(path.lastIndexOf(SEPARATOR)+1, path.length()));
	}

	@Override
	public java.nio.file.Path getParent() {
		// Might already be the root path
		if (SEPARATOR.equals(path)) return null;
		
		return new SofsPath(path.substring(0, path.lastIndexOf(SEPARATOR)));
	}

	@Override
	public int getNameCount() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path getName(int index) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path subpath(int beginIndex, int endIndex) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public boolean startsWith(java.nio.file.Path other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public boolean startsWith(String other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public boolean endsWith(java.nio.file.Path other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public boolean endsWith(String other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path normalize() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path resolve(java.nio.file.Path other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path resolve(String other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path resolveSibling(java.nio.file.Path other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path resolveSibling(String other) {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path relativize(java.nio.file.Path other) {
		// if this path is "/a/b" and the given path is "/a/b/c/d" then the resulting relative path would be "c/d"
		if ( !(other instanceof SofsPath) ) 
			throw new IllegalArgumentException("The other path provided is not an instance of " + getClass().getName());
		
		SofsPath otherPath = (SofsPath) other;
		if (!otherPath.absolute) 
			throw new IllegalArgumentException("The other path provided is not absolute");
		if (!this.absolute) 
			throw new IllegalArgumentException("This path is not absolute");
		
		String newPath = otherPath.path.replaceFirst( this.path , "");
		
		if (otherPath.path.equals(newPath)) {
			throw new IllegalArgumentException("The 2 paths provided cannot be relativized");
		}
		
		String relativeNewPath = newPath.substring(1);
		
		return new SofsPath(relativeNewPath);
	}

	@Override
	public URI toUri() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path toAbsolutePath() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public java.nio.file.Path toRealPath(LinkOption... options)
			throws IOException {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public File toFile() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public Iterator<java.nio.file.Path> iterator() {
		throw new UnsupportedOperationException("This is not implemented");
	}

	@Override
	public int compareTo(java.nio.file.Path other) {
		throw new UnsupportedOperationException("This is not implemented");
	}
	
	@Override
	public boolean equals(Object other) {
		if ( !(other instanceof SofsPath)) return false;
		SofsPath otherPath = (SofsPath)other;
		return this.path.equals(otherPath.path);
	}

	@Override
	public String toString() {
		return "[SofsPath: " + path + "]";
	}
	
}
