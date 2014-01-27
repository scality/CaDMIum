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
package com.scality.cdmi.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.impl.metadata.CdmiMetadata;
import com.scality.cdmi.impl.metadata.CdmiMetadataReader;

/**
 * An {@link InputStream} implementation specific for reading CDMI data objects.
 * It hides in the background the GET request to the CDMI server and
 * provides a stream view. The GET request is nonCDMI for performance
 * reasons, and the contents are streamed directly from the partial response
 * from the server. In effect this stream is not repeatable.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class CdmiInputStream extends InputStream {
    private CdmiConnector connector;
    private String path;
    private long pos_in_target;
    private long final_pos;
    private boolean closed;
    private InputStream in;

    /**
     * Constructor only used in tests.
     */
    protected CdmiInputStream() {
    }

    public CdmiInputStream(String path, CdmiConnector conn) throws CdmiConnectionException, FileNotFoundException {
        this(path, conn, 0, -1);
    }
    
    public CdmiInputStream(String path, CdmiConnector conn, long startPos, int length)
            throws CdmiConnectionException, FileNotFoundException {
        this.connector = conn;
        this.pos_in_target = startPos;
        CdmiMetadataReader metareader = new CdmiMetadataReader(this.connector);
        CdmiMetadata meta = metareader.readMetadata(path);
        this.path = "cdmi_objectid/" + meta.getObjectID();
        if (length > 0) {
            this.final_pos = startPos + length;
            if (this.final_pos > meta.getSize()) {
                this.final_pos = meta.getSize();
            }
        } else {
            this.final_pos = meta.getSize();
        }
        this.closed = false;
        this.in = null;
    }

    /**
     * Connect to the server and read a fixed number of bytes.
     * 
     * @param toread
     *            number of bytes to read
     * @return byte array containing the contents that were read
     * @throws CdmiConnectionException
     */
    private InputStream connectorRead() throws CdmiConnectionException {
        try {
            HttpResponse response = connector.readObjectNonCdmi(path, pos_in_target, final_pos - pos_in_target);
            int status = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == status || HttpStatus.SC_PARTIAL_CONTENT == status) {
                return response.getEntity().getContent();
            } else {
                throw new CdmiConnectionException("Impossible to read path " + path
                        + " got response " + response.getStatusLine());
            }
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * Allow closing the input stream earlier to release connections.
     * 
     * @throws CdmiConnectionException
     */
    private void closeInputStream() throws CdmiConnectionException {
        if (in != null) {
            try {
                in.close();
                in = null;
            } catch (IOException e) {
                throw new CdmiConnectionException(e);
            }
        }
    }

    /**
     * Close the associated stream. close() can be called multiple times on the
     * same stream.
     */
    @Override
    public void close() throws IOException {
        closeInputStream();
        closed = true;
    }

    @Override
    public int available() throws IOException {
        if (closed) {
            throw new IOException("Stream has already been closed.");
        }
        return (int) Math.min(final_pos - pos_in_target, Integer.MAX_VALUE);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        // Error checking.
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Argument offset cannot be negative");
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Argument length cannot be negative");
        }
        if (b == null) {
            throw new NullPointerException("Destination buffer cannot be null");
        }
        if (length > b.length - offset) {
            throw new IndexOutOfBoundsException(
                    "Argument length cannot be greater than buffer length - offset");
        }
        if (closed) {
            throw new IOException("Input stream already closed");
        }
        if (length == 0) {
            return 0;
        }
        if (pos_in_target == final_pos && in == null) {
            // End of file.
            return -1;
        }
        if (in == null) {
            in = connectorRead();
        }
        int totalbytesread = 0;
        try {
            totalbytesread = in.read(b, offset, length);
            pos_in_target += totalbytesread;
        } catch (CdmiConnectionException e) {
            throw new IOException(e);
        }
        if (pos_in_target == final_pos) {
            closeInputStream();
        }
        return totalbytesread;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        if (closed) {
            throw new IOException("Stream already closed");
        }

       
        // Get the maximum number of bytes that can be skipped.
        long max_skippable_bytes;
        if (in == null) {
            max_skippable_bytes = final_pos - pos_in_target;
            long skipped = Math.min(n, max_skippable_bytes);
            pos_in_target += skipped;
            return skipped;
        } else {
            return in.skip(n);
        }
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Stream already closed");
        }
        if (pos_in_target == final_pos && in == null) {
            return -1;
        }
        if (in == null) {
            in = connectorRead();
        }
        int value = in.read();
        pos_in_target++;
        if (pos_in_target == final_pos) {
            closeInputStream();
        }
        return value;
    }
}