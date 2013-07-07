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
package com.scality.cdmi.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.scality.cdmi.api.CdmiAuthScope;
import com.scality.cdmi.api.CdmiClient;
import com.scality.cdmi.api.CdmiConnectionManager;
import com.scality.cdmi.api.CdmiCredentials;
import com.scality.cdmi.api.RequestFactory;

/**
 * Example code for writing to a data object in a CDMI repository.
 *
 * @author ziad.bizri@ezako.com for Scality
 */
public class Example {
    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length != 6) {
            System.err
                    .println("Usage: Example <uri> <version> <user> <password> <filename> <destination>");
            return;
        }

        // Parse the arguments.
        String uri = args[0];
        String version = args[1];
        String user = args[2];
        String password = args[3];
        String filename = args[4];
        String destination = args[5];
        
        // Copy the file to the destination.
        RequestFactory factory = RequestFactory.newCdmiFactory(new URI(uri), version);
        CdmiConnectionManager cm = CdmiConnectionManager.newPooledConnectionManager(factory,
                new CdmiAuthScope("localhost", 443), new CdmiCredentials(user, password));
        CdmiClient client = cm.getClient();
        InputStream in = client.open(destination);
        OutputStream out = new FileOutputStream(new File(filename));
        byte[] buffer = new byte[1 << 16];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.close();
        in.close();
        cm.shutdown();
    }
}
