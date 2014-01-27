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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author julien.muller@ezako.com for Scality
 * @since 1.7
 */
public class PathTest {

	@Test
	public void simpleRoot() {
		SofsPath p = new SofsPath("/");
		assertTrue(p.isAbsolute());
		assertEquals("/", p.path);
	}

	@Test
	public void simpleAbsolute() {
		SofsPath p = new SofsPath("/foo/bar");
		assertTrue(p.isAbsolute());
		assertEquals("/foo/bar", p.path);
	}

	@Test
	public void cleanUrl() {
		SofsPath p = new SofsPath("/foo/bar/");
		assertTrue(p.isAbsolute());
		System.out.println(p.path);
		assertEquals("/foo/bar", p.path);
	}

	@Test
	public void simpleRelative() {
		SofsPath p = new SofsPath("foo/bar");
		assertFalse(p.isAbsolute());
		assertEquals("foo/bar", p.getPath());
	}

	@Test
	public void fileName() {
		SofsPath p = new SofsPath("foo/bar.h");
		assertEquals(new SofsPath("bar.h"), p.getFileName());
	}

	@Test
	public void fileName2() {
		SofsPath p = new SofsPath("foo/bar");
		assertEquals(new SofsPath("bar"), p.getFileName());
	}

	@Test
	public void fileName3() {
		SofsPath p = new SofsPath("foo/bar/baz");
		assertEquals(new SofsPath("baz"), p.getFileName());
	}

	@Test
	public void parent() {
		SofsPath p = new SofsPath("/foo/bar/baz");
		assertEquals(new SofsPath("/foo/bar"), p.getParent());
	}

	@Test
	public void relativize() {
		SofsPath p = new SofsPath("/foo/bar/");
		SofsPath p2 = new SofsPath("/foo/bar/baz/buz");
		assertEquals(new SofsPath("baz/buz"), p.relativize(p2));
		p = new SofsPath("/foo/");
		p2 = new SofsPath("/foo/bar/baz");
		assertEquals(new SofsPath("bar/baz"), p.relativize(p2));
	}

	@Test
	public void startWith() {
		SofsPath p = new SofsPath("/foo/bar/");
		SofsPath p2 = new SofsPath("/foo/bar/baz/buz");
		assertTrue(p2.startsWith(p));
		assertTrue(p.startsWith(p));
		assertFalse(p.startsWith(p2));
		assertFalse(p.startsWith((SofsPath) null));
	}
	
	@Test
	public void resolve() {
		SofsPath p = new SofsPath("/foo/bar");
		assertEquals(new SofsPath("/foo/bar"), p.resolve(""));
		assertEquals(new SofsPath("/foo/bar"), p.resolve(new SofsPath("")));
		assertEquals(new SofsPath("/foo/bar"), p.resolve((SofsPath)null));
		SofsPath p2 = new SofsPath("/baz");
		assertEquals(p.resolve(p2), p2);
		p2 = new SofsPath("baz");
		assertEquals(p.resolve(p2), new SofsPath("/foo/bar/baz"));
		p2 = new SofsPath("baz/");
		assertEquals(p.resolve(p2), new SofsPath("/foo/bar/baz"));
		p2 = new SofsPath("/");
		assertEquals(p.resolve(p2), p2);
		p = new SofsPath("/foo/bar/");
		p2 = new SofsPath("baz/");
		assertEquals(p.resolve(p2), new SofsPath("/foo/bar/baz"));
		
		p = new SofsPath("");
		assertEquals(p2, p.resolve(p2));
		
		p = new SofsPath("bar/baz/foo");
		assertEquals(new SofsPath("bar/baz/foo/baz"), p.resolve(p2));
	}
	
}
