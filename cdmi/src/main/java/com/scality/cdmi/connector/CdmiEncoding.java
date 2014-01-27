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

import com.scality.cdmi.api.CdmiConnectionException;


/**
 * List the encodings supported for CDMI operations.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public final class CdmiEncoding {
    /**
     * UTF-8: encoding used for text files, there is no additional encoding.
     */
    public final static String UTF8 = "utf-8";
    /**
     * BASE64: encode in base-64 all the data transferred to the CDMI server.
     * Adds a penalty of 4/3 in the amount of data to be transferred.
     */
    public final static String BASE64 = "base64";

    /**
     * Helper function to check whether the specified encoding is supported.
     * 
     * @param encoding string representation of the desired transport encoding
     * @throws CdmiConnectionException if the encoding is not supported
     */
    public final static void checkEncodingOrFail(String encoding) throws CdmiConnectionException {
        if (!BASE64.equals(encoding) && !UTF8.equals(encoding)) {
            throw new CdmiConnectionException("Unsupported encoding " + encoding);
        }
    }
}
