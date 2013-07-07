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

import java.io.FileInputStream;
import java.io.IOException;

import com.scality.cdmi.connector.CdmiInputStream;

/**
 * A mock {@link CdmiInputStream} object for reading data objects. It is mapped
 * to the local filesystem.
 *
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class MockCdmiInputStream extends CdmiInputStream {
    private FileInputStream fis;
    private int maxRead;
    private int read;

    public MockCdmiInputStream(FileInputStream fis, int maxRead) {
        this.fis = fis;
        this.maxRead = maxRead;
        this.read = 0;
    }

    @Override
    public void close() throws IOException {
        fis.close();
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (maxRead > -1 && length > (maxRead - read)) {
            length = maxRead - read;
        }
        int result = fis.read(b, offset, length);
        read += result;
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        byte[] tmpbuffer = new byte[(int)n];
        read = 0;  // reinitialize.
        return fis.read(tmpbuffer, 0, (int) n);
    }

    @Override
    public int read() throws IOException {
        if (maxRead > -1 && read == maxRead) {
            return -1;
        }
        read++;
        return fis.read();
    }

    @Override
    public int available() throws IOException {
        return fis.available();
    }
}