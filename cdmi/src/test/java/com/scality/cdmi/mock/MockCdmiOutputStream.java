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
package com.scality.cdmi.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.connector.CdmiOutputStream;

/**
 * A mock {@link CdmiOutputStream} stream useful for testing. It maps files to
 * the local filesystem.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class MockCdmiOutputStream extends CdmiOutputStream {
    /**
     * The {@link RandomAccessFile} allow writing at a specific offset.
     */
    private RandomAccessFile ras;

    public MockCdmiOutputStream(File file) throws FileNotFoundException {
        this.ras = new RandomAccessFile(file, "rw");
    }

    @Override
    public void write(byte[] b, int offset, int length) throws CdmiConnectionException {
        try {
            ras.write(b, offset, length);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    @Override
    public void write(byte[] b) throws CdmiConnectionException {
        try {
            ras.write(b);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    @Override
    public void write(int b) throws CdmiConnectionException {
        try {
            ras.write(b);
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    @Override
    public void flush() throws CdmiConnectionException {
    }

    @Override
    public void close() throws CdmiConnectionException {
        try {
            ras.close();
        } catch (IOException e) {
            throw new CdmiConnectionException(e);
        }
    }

    /**
     * Allow repositioning the stream.
     * 
     * @param newPosition
     *            the new position in the stream.
     * @throws IOException
     */
    public void setPos(long newPosition) throws IOException {
        ras.getChannel().position(newPosition);
    }
}
