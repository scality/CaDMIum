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

import java.io.IOException;

/**
 * Wrapper around all exceptions that can occur during the communication with a CDMI server.
 * 
 * @author ziad.bizri@ezako.com for Scality
 *
 */
public class CdmiConnectionException extends IOException {
    /**
     * Automatically generated.
     */
	private static final long serialVersionUID = -9038407005267847257L;
	
	/**
	 * Default Constructor with null as its error detail message
	 */
	public CdmiConnectionException() { 
		super();
	}
	
	/**
	 * Constructor with detail message
	 * @param message
	 */
	public CdmiConnectionException(String message) {
		super(message);
	}
	
	/**
	 * Constructor with detail message and cause
	 * @param message
	 * @param cause
	 */
	public CdmiConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor with cause
	 * @param cause
	 */
	public CdmiConnectionException(Throwable cause) {
		super(cause);
	}
}
