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
package com.scality.sofs.utils;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * An exception while processing a payload
 * 
 * @see com.scality.sofs.utils.GeoSyncPayLoadProcessor
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class GeoSyncPayLoadProcessingException extends Exception {

	private static final long serialVersionUID = 1L;

	// Error code, preferably matching HTTP. Defaults to 500 if nothing better
	protected int errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

	/**
	 * Default Constructor
	 */
	public GeoSyncPayLoadProcessingException() {
		super();
	}

	/**
	 * Constructor from parent
	 * 
	 * @param message
	 */
	public GeoSyncPayLoadProcessingException(String message) {
		super(message);
	}

	/**
	 * Constructor from parent
	 * 
	 * @param cause
	 */
	public GeoSyncPayLoadProcessingException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor from parent
	 * 
	 * @param message
	 * @param cause
	 */
	public GeoSyncPayLoadProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param errorCode
	 */
	public GeoSyncPayLoadProcessingException(String message, Throwable cause,
			int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * An error code for more details. Preferably matching httperrors.
	 */
	public int getErrorCode() {
		return errorCode;
	}

}
