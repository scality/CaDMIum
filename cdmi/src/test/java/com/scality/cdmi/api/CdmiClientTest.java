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
package com.scality.cdmi.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.scality.cdmi.mock.MockCdmiClient;

/**
 * Regression test.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public class CdmiClientTest {
	private static String BASEDIR = "/testdirs/";

	protected CdmiClient client;

	protected CdmiClient getTestClient() {
		return new MockCdmiClient();
	}

	@Before
	public void setUp() throws Exception {
		client = getTestClient();
		client.delete(BASEDIR, true /* recursive */);
		client.makedir(BASEDIR);
	}

	@After
	public void tearDown() throws Exception {
		client.delete(BASEDIR, true /* recursive */);
	}

	private int readAllContentsToBuffer(InputStream in, byte[] buffer)
			throws IOException {
		return readSomeContentsToBuffer(in, buffer, buffer.length);
	}

	// Utility for reading a stream up to a specific length.
	private int readSomeContentsToBuffer(InputStream in, byte[] buffer,
			int length) throws IOException {
		int read = 0;
		int offset = 0;
		int remaininglenth = length;
		int lastread;
		while ((lastread = in.read(buffer, offset, remaininglenth)) > 0) {
			offset += lastread;
			remaininglenth -= lastread;
			read += lastread;
		}
		return read;
	}

	/**
	 * Test dots at the beginning of dirnames.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPoint() throws IOException {
		String key = BASEDIR + "foo/.cat";
		Assert.assertFalse(client.exists(key));
		Assert.assertTrue(client.makedirs(key));
		Assert.assertTrue(client.exists(key));
		FileMetadata status = client.getMetadata(key);
		Assert.assertEquals(BASEDIR + "foo/.cat/", status.getKey());

		Assert.assertTrue(client.delete(key, false));
	}

	/**
	 * Test touching a file
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTouch() throws IOException {
		String path = BASEDIR + "foo.txt";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.touch(path));
		Assert.assertTrue(client.exists(path));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testGetMetadataEmpty() throws IOException {
		String path = BASEDIR + "cat.txt";
		Assert.assertFalse(client.exists(path));
		boolean exceptionRaised = false;
		try {
			@SuppressWarnings("unused")
			FileMetadata meta = client.getMetadata(path);
		} catch (FileNotFoundException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	/**
	 * Test getting metadata of a file
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetMetadata() throws IOException {
		// Try on a file.
		String path = BASEDIR + "bar.txt";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.touch(path));
		FileMetadata meta = client.getMetadata(path);
		Assert.assertEquals(0L, meta.getLength());
		Assert.assertFalse(meta.isContainer());
		Assert.assertEquals(path, meta.getKey());
		OutputStream out = client.append(path);
		out.write(new byte[] { 42, 43, 44 });
		out.close();
		meta = client.getMetadata(path);
		Assert.assertEquals(3L, meta.getLength());
		// Try on a container.
		path = BASEDIR + "foo/";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.makedir(path));
		meta = client.getMetadata(path);
		Assert.assertEquals(-1, meta.getLength());
		Assert.assertTrue(meta.isContainer());
		Assert.assertEquals(path, meta.getKey());
		// Try on a container with no ending slash.
		path = BASEDIR + "bar";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.makedir(path));
		meta = client.getMetadata(path);
		Assert.assertEquals(-1, meta.getLength());
		Assert.assertTrue(meta.isContainer());
		Assert.assertEquals(path + "/", meta.getKey());
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testListMetadata() throws IOException {
		String path = BASEDIR + "quux/";
		TreeSet<String> filenames = new TreeSet<String>();
		filenames.addAll(Arrays.asList("foo", "bar", "baz"));
		TreeSet<String> dirnames = new TreeSet<String>();
		dirnames.addAll(Arrays.asList("cat", "car"));
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.makedir(path));

		for (String name : filenames) {
			Assert.assertTrue(client.touch(path + name));
		}
		for (String name : dirnames) {
			Assert.assertTrue(client.makedir(path + name + "/"));
		}

		FileMetadata[] allmeta = client.listMetadata(path);
		Assert.assertEquals(dirnames.size() + filenames.size(), allmeta.length);

		for (FileMetadata meta : client.listMetadata(path)) {
			File file = new File(meta.getKey());
			String basename = file.getName();
			if (meta.isContainer()) {
				Assert.assertEquals(-1, meta.getLength());
				dirnames.remove(basename);
			} else {
				Assert.assertEquals(0, meta.getLength());
				filenames.remove(basename);
			}
		}
		Assert.assertTrue(filenames.isEmpty());
		Assert.assertTrue(dirnames.isEmpty());
	}

	@Test
	public void testListEmptyFile() throws IOException {
		String path = BASEDIR + "quux";
		Assert.assertFalse(client.exists(path));
		boolean exceptionRaised = false;
		try {
			client.listMetadata(path);
		} catch (FileNotFoundException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue("Operation should raise a FileNotFound exception",
				exceptionRaised);
	}
	
	@Test
	public void testListEmptyDir() throws IOException {
		String path = BASEDIR + "quux/";
		Assert.assertFalse(client.exists(path));
		boolean exceptionRaised = false;
		try {
			client.listMetadata(path);
		} catch (FileNotFoundException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue("Operation should raise a FileNotFound exception",
				exceptionRaised);
		Assert.assertTrue(client.makedir(path));
		FileMetadata[] meta = client.listMetadata(path);
		Assert.assertEquals(0, meta.length);
	}


	@Test
	public void testWriting() throws IOException {
		// Write a first string.
		String path = BASEDIR + "cat.txt";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisateststring";
		out.write(teststring.getBytes());
		out.close();

		int pos = teststring.length();
		FileMetadata meta = client.getMetadata(path);
		Assert.assertEquals(pos, meta.getLength());
		Assert.assertFalse(meta.isContainer());
		Assert.assertEquals(path, meta.getKey());

		InputStream is = client.open(path);
		byte[] buff = new byte[pos];
		Assert.assertEquals(pos, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals(teststring, new String(buff, 0, pos));

		// Add an additional string.
		out = client.append(path);
		String additionalstring = "additional";
		String finalstring = teststring + additionalstring;
		out.write(additionalstring.getBytes());
		out.close();

		pos = finalstring.length();
		meta = client.getMetadata(path);
		Assert.assertEquals(pos, meta.getLength());
		Assert.assertFalse(meta.isContainer());
		Assert.assertEquals(path, meta.getKey());

		is = client.open(path);
		buff = new byte[pos];
		Assert.assertEquals(pos, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals(finalstring, new String(buff, 0, pos));

		// Now try to write over.
		out = client.write(path, 6L);
		String replace = "replace";
		out.write(replace.getBytes());
		out.close();

		// Meta is unchanged
		meta = client.getMetadata(path);
		Assert.assertEquals(pos, meta.getLength());
		Assert.assertFalse(meta.isContainer());
		Assert.assertEquals(path, meta.getKey());

		// Read again
		is = client.open(path);
		buff = new byte[pos];
		Assert.assertEquals(pos, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals("thisisreplaceringadditional", new String(buff, 0, pos));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testWritingAndOther() throws IOException {
		String path = BASEDIR + "bas.txt";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0);
		out.write("foo".getBytes());
		Assert.assertTrue(client.exists(path));
		out.write("bar".getBytes());
		FileMetadata meta = client.getMetadata(path);
		Assert.assertEquals(path, meta.getKey());
		out.close();
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testReadEmptyFile() throws IOException {
		String path = BASEDIR + "cat.txt";
		Assert.assertFalse(client.exists(path));
		boolean exceptionRaised = false;
		try {
			@SuppressWarnings("unused")
			InputStream in = client.open(path);
		} catch (FileNotFoundException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testReading() throws IOException {
		// Write a first string.
		String path = BASEDIR + "car.txt";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisateststring";
		out.write(teststring.getBytes());
		out.close();

		int size = teststring.length();
		InputStream is = client.open(path);
		byte[] buff = new byte[size];
		Assert.assertEquals(size, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals(teststring, new String(buff, 0, size));

		// Now read from an offset
		is = client.open(path, 5L);
		size -= 5;
		buff = new byte[size];
		Assert.assertEquals(size, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals("sateststring", new String(buff, 0, size));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testSkip() throws IOException {
		String path = BASEDIR + "bar.dat";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisamuchlongerteststring";
		out.write(teststring.getBytes());
		out.close();

		// Test that we can skip before the first read.
		InputStream in = client.open(path);
		Assert.assertEquals(6, in.skip(6));
		Assert.assertEquals(4, in.skip(4));
		int size = 10;
		byte[] buff = new byte[size];
		Assert.assertEquals(size, readAllContentsToBuffer(in, buff));
		in.close();
		Assert.assertEquals("hlongertes", new String(buff, 0, size));

		// Test that we can skip after the first read.
		in = client.open(path);
		Assert.assertEquals(6, readSomeContentsToBuffer(in, buff, 6));
		Assert.assertEquals(4, in.skip(4));
		Assert.assertEquals(10, readSomeContentsToBuffer(in, buff, 10));
		in.close();
		Assert.assertEquals("hlongertes", new String(buff, 0, size));

		// Test the skip beyond limit.
		in = client.open(path);
		in.skip(200);
		// assertEquals(teststring.length(), in.skip(200));
		Assert.assertEquals(-1, in.read(buff, 0, size));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testCopy() throws IOException {
		String path = BASEDIR + "bar.bat";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisaveryverylongteststring";
		out.write(teststring.getBytes());
		out.close();

		String dest_path = BASEDIR + "bar.bat2";
		out = client.write(dest_path, 0);
		InputStream in = client.open(path);
		byte buf[] = new byte[teststring.length()];
		int bytesRead = in.read(buf);
		while (bytesRead >= 0) {
			out.write(buf, 0, bytesRead);
			bytesRead = in.read(buf);
		}
		in.close();
		out.close();

		// Now check the contents
		in = client.open(dest_path);
		buf = new byte[teststring.length()];
		Assert.assertEquals(teststring.length(), readAllContentsToBuffer(in, buf));
		in.close();
		Assert.assertEquals(teststring, new String(buf));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMove() throws IOException {
		String path = BASEDIR + "quux.txt";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisateststring";
		out.write(teststring.getBytes());
		out.close();

		String pathTo = BASEDIR + "move.txt";
		Assert.assertFalse(client.exists(pathTo));
		Assert.assertTrue(client.rename(path, pathTo));
		Assert.assertTrue(client.exists(pathTo));
		Assert.assertFalse(client.exists(path));

		// Now read from an offset
		InputStream is = client.open(pathTo);
		int size = teststring.length();
		byte[] buff = new byte[size];
		Assert.assertEquals(size, readAllContentsToBuffer(is, buff));
		is.close();
		Assert.assertEquals(teststring, new String(buff, 0, size));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMoveToParent() throws IOException {
		String dir = BASEDIR + "bar/.baz/.cat/";
		Assert.assertTrue(client.makedirs(dir));
		String path = dir + "quux.txt";
		Assert.assertTrue(client.touch(path));
		OutputStream out = client.write(path, 0L);
		String teststring = "thisisateststring";
		out.write(teststring.getBytes());
		out.close();

		String parentpath = BASEDIR + "bar/.baz/quux.txt";
		Assert.assertTrue(client.rename(path, parentpath));
		Assert.assertTrue(client.exists(parentpath));
		FileMetadata meta = client.getMetadata(parentpath);
		Assert.assertEquals(17, meta.getLength());
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMoveDir() throws IOException {
		String path1 = BASEDIR + "dir1";
		String file1 = path1 + "/foo";
		String path2 = BASEDIR + "dir2";
		Assert.assertFalse(client.exists(path1));
		Assert.assertFalse(client.exists(path2));
		Assert.assertTrue(client.makedir(path1));
		Assert.assertTrue(client.touch(file1));
		Assert.assertTrue(client.rename(path1, path2));
		Assert.assertFalse(client.exists(path1));
		Assert.assertTrue(client.exists(path2));
		Assert.assertFalse(client.exists(file1));
		Assert.assertTrue(client.exists(file1.replace("dir1", "dir2")));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMakeDir() throws IOException {
		String path = BASEDIR + "foo/bar/baz/cat/quux/";
		Assert.assertFalse(client.makedir(path));
		Assert.assertFalse(client.exists(BASEDIR + "foo/"));

		path = BASEDIR + "bar/";
		Assert.assertTrue(client.makedir(path));
		Assert.assertTrue(client.exists(path));

		path = BASEDIR + "noslash";
		Assert.assertTrue(client.makedir(path));
		Assert.assertTrue(client.exists(path));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMakeDirs() throws IOException {
		String path = BASEDIR + "foo/bar/baz/cat/quux/";
		Assert.assertTrue(client.makedirs(path));
		// Fails the second time.
		Assert.assertFalse(client.makedirs(path));
		Assert.assertTrue(client.exists(BASEDIR + "foo/"));
		Assert.assertTrue(client.exists(BASEDIR + "foo/bar/baz/"));
		Assert.assertFalse(client.makedirs(BASEDIR + "foo/bar/baz"));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testMakeDirsException() throws IOException {
		String path = BASEDIR + "foo/bar";
		Assert.assertTrue(client.makedir(BASEDIR + "foo"));
		Assert.assertTrue(client.touch(path));
		String fulldir = path + "/baz/cat/";
		boolean exceptionRaised = false;
		try {
			client.makedirs(fulldir);
		} catch (CdmiConnectionException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testDelete() throws IOException {
		String path = BASEDIR + "foo.dat";
		Assert.assertFalse(client.exists(path));
		Assert.assertTrue(client.touch(path));
		Assert.assertTrue(client.exists(path));
		Assert.assertTrue(client.delete(path, false));
		Assert.assertFalse(client.exists(path));
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testDeleteContainer() throws IOException {
		String path = BASEDIR + "bar";
		Assert.assertFalse(client.exists(path));
		Assert.assertFalse(client.delete(path, false));
		Assert.assertTrue(client.makedir(path));
		Assert.assertTrue(client.delete(path, false));

		Assert.assertTrue(client.makedir(path));
		Assert.assertTrue(client.touch(path + "/foo.dat"));
		boolean exceptionRaised = false;
		try {
			client.delete(path, false);
		} catch (IOException e) {
			exceptionRaised = true;
		}
		Assert.assertTrue(exceptionRaised);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testPutAndGet() throws IOException {
		String path = BASEDIR + "testPut";
		File testFile = File.createTempFile("tempfile", "testPut");
		File outputFile = File.createTempFile("tempfile", "testGet");
		BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
		final String contents = "This is a test file that is going to be sent out.";
		writer.write(contents);
		writer.close();
		Assert.assertFalse(client.get(path, outputFile));
		Assert.assertTrue(client.put(path, testFile));
		Assert.assertFalse(client.put(path, testFile)); // File already exists.
		Assert.assertTrue(client.exists(path));
		Assert.assertEquals(contents.length(), client.getMetadata(path).getLength());

		// Now let's get it.
		Assert.assertTrue(client.get(path, outputFile));
		Assert.assertEquals(contents, new Scanner(outputFile).nextLine());
	}

	/**
	 * Regression test: make sure that we can get the metadata of the root
	 * directory.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRootdir() throws IOException {
		FileMetadata meta = client.getMetadata("/");
		Assert.assertEquals("/", meta.getKey());
		Assert.assertTrue(meta.isContainer());
	}

	/**
	 * Make sure that path are html escaped.
	 */
	@Test
	public void testEscape() throws IOException {
		String path = BASEDIR + "test%bar<quux>";
		Assert.assertTrue(client.touch(path));
		Assert.assertTrue(client.exists(path));
		Assert.assertTrue(client.delete(path, false));
	}
	
	/**
	 * Make sure we support special chars in filenames
	 * @throws IOException
	 */
	@Test
	public void testSpecialCharInFileNames() throws IOException {
		String path = BASEDIR + "Encyclopædia";
		Assert.assertTrue(client.touch(path));
		Assert.assertTrue(client.exists(path));
		Assert.assertTrue(client.delete(path, false));
	}
}
