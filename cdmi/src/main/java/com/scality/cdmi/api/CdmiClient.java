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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.scality.cdmi.connector.CdmiInputStream;
import com.scality.cdmi.connector.CdmiOutputStream;

/**
 * A client interface for operations similar to that of a filesystem.
 * 
 * Example (get a remote file and delete it afterwards):
 * 
 * <pre>
 * {@code
 *  RequestFactory factory = RequestFactory.newCdmiFactory(
 *      URI.create("http://localhost:80"), "1.0.1");
 *  CdmiConnectionManager cm = CdmiConnectionManager.newPooledConnectionManager(
 *      factory,
 *      new CdmiAuthScope("localhost", 443),
 *      new CdmiCredentials("root", "root"));
 *  CdmiClient client = cm.getClient();
 *  if (client.exists("/path/to/remote/data/object")) {
 *      client.get("/path/to/remote/data/object", new File("path/to/localfile"));
 *      client.delete("/path/to/remote/data/object");
 *  }
 *  cm.shutdown();
 * }
 * </pre>
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public interface CdmiClient {
    /**
     * Copy a local file to a data object in the CDMI repository.
     * 
     * @param key
     *            the name of the target in the repository.
     * @param file
     *            the {@link File} to be copied.
     * @return true if the file in the repository was created successfully;
     *         false if the key already exists.
     * @throws IOException
     *             if any error occurred during communication.
     */
    boolean put(String key, File file) throws IOException;

    /**
     * Read a data object in the CDMI repository to a local file.
     * 
     * @param key
     *            the name of the source in the repository.
     * @param destFile
     *            the destination {@link File}.
     * @return true if the local file was created; false if it already exists.
     * @throws IOException
     *             if any error occurred during communication.
     */
    boolean get(String key, File destFile) throws IOException;

    /**
     * Create a new data object in the CDMI repository of size 0.
     * 
     * @param key
     *            the name of the target in the repository.
     * @return true if the data object was created; false it already exists.
     * @throws IOException
     *             if any error occurred during communication.
     */
    boolean touch(String key) throws IOException;

    /**
     * Check whether a data object or a container exist in the CDMI repository.
     * 
     * @param key
     *            the name of the target in the repository.
     * @return true if the data object or the container exists; false otherwise.
     * @throws IOException
     *             if any error occurred during communication.
     */
    boolean exists(String key) throws IOException;

    /**
     * Create a new {@link CdmiInputStream} for reading a data object stored in
     * the CDMI repository.
     * 
     * @param key
     *            the name of the data object in the repository.
     * @return a {@link CdmiInputStream} object used for reading.
     * @throws IOException
     *             if the name does not exist or if any error occurred during
     *             communication.
     */
    InputStream open(String key) throws IOException;

    /**
     * Create a new {@link CdmiInputStream} for reading a data object stored in
     * the CDMI repository starting at a byte offset.
     * 
     * @param key
     *            the name of the data object in the repository.
     * @param startPos
     *            the offset into the data object.
     * @return a {@link CdmiInputStream} object used for reading.
     * @throws IOException
     *             if the name does not exist or if any error occurred during
     *             communication.
     */
    InputStream open(String key, long startPos) throws IOException;

    /**
     * Create a new {@link CdmiInputStream} for reading a data object stored in
     * the CDMI repository starting at a byte offset.
     * 
     * @param key
     *            the name of the data object in the repository.
     * @param startPos
     *            the offset into the data object.
     * @param maxRead
     *            the maximum length of the contents to be downloaded.
     * @return a {@link CdmiInputStream} object used for reading.
     * @throws IOException
     *             if the name does not exist or if any error occurred during
     *             communication.
     */
    InputStream open(String key, long startPos, int maxRead) throws IOException;

    /**
     * Create a new {@link CdmiOutputStream} for writing to a data object stored
     * in the CDMI repository, starting at a specified offset.
     * 
     * @param key
     *            the name of the data object in the repository.
     * @param startPos
     *            the start position for the write.
     * @return a {@link CdmiOutputStream} object used for writing.
     * @throws IOException
     *             if any error occurred during communication.
     */
     OutputStream write(String key, long startPos) throws IOException;

    /**
     * Create a new {@link CdmiOutputStream} for appending to a data object
     * stored in the CDMI repository.
     * 
     * @param key
     *            the name of the data object in the repository.
     * @return a {@link CdmiOutputStream} object used for writing.
     * @throws IOException
     *             if any error occurred during the communication.
     */
     OutputStream append(String key) throws IOException;

    /**
     * Delete a container or a data object from the CDMI repository.
     * 
     * @param key
     *            the name of the data object or the container in the
     *            repository.
     * @param recursive
     *            true if the contents of a container must be also deleted; does
     *            not affect the deletion of a data object.
     * @return true if the deletion succeeded; false if the target does not
     *         exists.
     * @throws IOException
     *             if the delete is not recursive and the target container is
     *             not empty, or if any error occurred during the communication.
     */
    boolean delete(String key, boolean recursive) throws IOException;

    /**
     * Move a data object or a container in the CDMI repository.
     * 
     * @param srcKey
     *            the name of the source data object or container in the
     *            repository.
     * @param dstKey
     *            the name of the target data object or container in the
     *            repository.
     * @return true if the move was successful.
     * @throws IOException
     *             if any error occurred during the communication.
     */
    boolean move(String srcKey, String dstKey) throws IOException;

    /**
     * Create a new container in the CDMI repository.
     * 
     * @param key
     *            the name of the container.
     * @return true if a new container was created; false otherwise.
     * @throws IOException
     *             if a data object with the same name already exists, or if any
     *             error occurred during communication.
     */
    boolean makedir(String key) throws IOException;

    /**
     * Create a new container in the CDMI repository, and create all the parent
     * containers in the path.
     * 
     * @param key
     *            the name of the container
     * @return true if a new container was created; false otherwise.
     * @throws IOException
     *             if a data object with same name already exists, or if any
     *             error occurred during the communication.
     */
    boolean makedirs(String key) throws IOException;

    /**
     * Read the metadata associated with a data object or a container.
     * 
     * @param key
     *            the name of the container or the data object in the CDMI
     *            repository.
     * @return a {@link FileMetadata} object containing the metadata.
     * @throws IOException
     *             if any error occurred during communication, or if the key
     *             does not exist.
     */
    FileMetadata getMetadata(String key) throws IOException;

    /**
     * Read the metadata associated with all the children of a container in the
     * CDMI repository. This method can also be used with data objects.
     * 
     * @param key
     *            the name of the container or the data object in the CDMI
     *            repository.
     * @return an array of {@link FileMetadata} for each of the children of a
     *         container, or an array with a single entry if the key names a
     *         data object. An empty array if there is no container or data
     *         object associated with this key.
     * @throws IOException
     *             if any error occurred during communication, or if the key
     *             does not exist.
     */
    FileMetadata[] listMetadata(String key) throws IOException;

    /**
     * Allow a user to run specific query for a key stored in the CDMI
     * repository.
     * 
     * @param key
     *            the name of the container or the data object in the CDMI
     * @param query
     *            the query string to pass to the request.
     * @return a String containing the contents of the response.
     * @throws IOException
     *             if any error occurred during communication.
     */
    String userExtension(String key, String query) throws IOException;
}
