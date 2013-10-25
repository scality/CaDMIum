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

import java.nio.file.WatchEvent.Modifier;

public enum SofsWatchEventModifier implements Modifier {

	/**
	 * The FILE_TREE modifier makes a WatchKey recursive. Without this modifier,
	 * a file watch is shallow: For a watched directory foo, WatchEvents are
	 * only generated for direct children such as foo/bar or foo/oof. For a
	 * changes to files in a subdirectory of foo, such as foo/adir/file will
	 * only be reported if the FILE_TREE modifier is specified. Note that this
	 * modifier is only available on the SOFS platform. If specified on other
	 * platforms, Path.register() will throw an UnsupportedOperationException.
	 */
	FILE_TREE,

	/**
	 * The CLOSE_CONN_ON_OVERFLOW modifier indicates that the WatchKey will send
	 * an error in case of overflow of its eventsList. In the default behavior,
	 * it will simply add an overflow event and miss events until slots are
	 * available in the eventsList
	 */
	CLOSE_CONN_ON_OVERFLOW,
}
