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
package com.scality.cdmi.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import com.scality.cdmi.api.CdmiClient;
import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.api.FileMetadata;
import com.scality.cdmi.api.RequestFactory;
import com.scality.cdmi.api.RetryStrategy;
import com.scality.cdmi.connector.CdmiConnector;
import com.scality.cdmi.connector.CdmiInputStream;
import com.scality.cdmi.connector.CdmiOutputStream;
import com.scality.cdmi.connector.CdmiTypes;
import com.scality.cdmi.impl.metadata.CdmiMetadata;
import com.scality.cdmi.impl.metadata.CdmiMetadataReader;
import com.scality.cdmi.impl.utils.KeyUtils;
import com.scality.cdmi.impl.utils.ParsingUtils;

/**
 * @author julien.muller@ezako.com for Scality
 *
 */
public class CdmiClientImpl implements CdmiClient {
    private CdmiConnector connector;
    private CdmiMetadataReader metadatareader;
    private int ioBufferSize;
    private int maxPutSize;
    private ParsingUtils parser;

    /**
     * Constructor
     * @param httpClient
     * @param factory
     * @param retryStrategy
     * @param ioBufferSize
     * @param maxPutSize
     * @param multiThreaded
     */
    public CdmiClientImpl(HttpClient httpClient, RequestFactory factory,
            RetryStrategy retryStrategy, int ioBufferSize, int maxPutSize,
            boolean multiThreaded) {
        this.connector = new CdmiConnector(factory, httpClient, retryStrategy, multiThreaded);
        this.metadatareader = new CdmiMetadataReader(connector);
        this.ioBufferSize = ioBufferSize;
        this.maxPutSize = maxPutSize;
        this.parser = new ParsingUtils();
    }

    @Override
    public boolean put(String key, File file) throws IOException {
        if (exists(key)) {
            return false; // File already exists.
        }
        FileInputStream is = new FileInputStream(file.getPath());
        BufferedInputStream buff = new BufferedInputStream(is);
        CdmiOutputStream os = new CdmiOutputStream(key, 0L, connector, maxPutSize);

        int length;
        byte[] data = new byte[ioBufferSize];
        while ((length = buff.read(data)) != -1) {
            os.write(data, 0, length);
        }
        os.close();
        buff.close();

        return true;
    }

    @Override
    public boolean get(String key, File destFile) throws IOException {
        if (!exists(key)) {
            return false; // File does not exist.
        }
        FileOutputStream out = new FileOutputStream(destFile.getPath());
        CdmiInputStream is = new CdmiInputStream(key, connector);

        int length;
        byte[] data = new byte[ioBufferSize];
        while ((length = is.read(data)) != -1) {
            out.write(data, 0, length);
        }
        is.close();
        out.close();

        return true;
    }

    @Override
    public boolean touch(String key) throws IOException {
        if (exists(key)) {
            return false; // File already exists.
        }
        HttpResponse response = connector.createEmptyObjectNonCdmi(key);
        EntityUtils.consumeQuietly(response.getEntity());
        return HttpStatus.SC_CREATED == response.getStatusLine().getStatusCode();
    }

    @Override
    public boolean exists(String key) throws IOException {
        HttpResponse response = connector.getObjectType(key);
        String result = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return false;
        }
        // Now parse the result.
        String objectType = parser.extractField(result, "objectType");
        if (CdmiTypes.CDMI_CONTAINER.equals(objectType) || CdmiTypes.CDMI_OBJECT.equals(objectType)) {
            return true;
        }
        // Hide non containers / objects.
        return false;
    }

    @Override
    public CdmiInputStream open(String key) throws IOException {
        return new CdmiInputStream(key, connector);
    }

    @Override
    public CdmiInputStream open(String key, long startPos) throws IOException {
        return new CdmiInputStream(key, connector, startPos, -1);
    }
    
    @Override
    public CdmiInputStream open(String key, long startPos, int maxRead) throws IOException {
        return new CdmiInputStream(key, connector, startPos, maxRead);
    }

    @Override
    public CdmiOutputStream write(String key, long startPos) throws IOException {
        return new CdmiOutputStream(key, startPos, connector, maxPutSize);
    }

    @Override
    public CdmiOutputStream append(String key) throws IOException {
        CdmiMetadata metadata = metadatareader.readMetadata(key);
        return new CdmiOutputStream(key, metadata.getSize(), connector, maxPutSize);
    }

    @Override
    public boolean delete(String key, boolean recursive) throws IOException {
        if (!exists(key)) {
            return false;
        }
        FileMetadata metadata = getMetadata(key);
        if (metadata.isContainer() && !key.endsWith("/")) {
            key += "/";
        }
        if (metadata.isContainer() && !recursive) {
            Iterator<String> it = getChildren(key).iterator();
            if (it.hasNext()) {
                throw new IOException("Cannot delete non empty container.");
            }
        }
        HttpResponse response = connector.delete(key);
        EntityUtils.consume(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode == HttpStatus.SC_NO_CONTENT;
    }

    @Override
    public boolean rename(String srcKey, String dstKey) throws IOException {
        FileMetadata status = getMetadata(srcKey);
        HttpResponse response;
        if (status.isContainer()) {
            if (!srcKey.endsWith("/")) {
                srcKey += "/";
            }
            if (!dstKey.endsWith("/")) {
                dstKey += "/";
            }
            if (dstKey.equals(srcKey)) {
                // Nothing to do.
                return true;  
            } else if (dstKey.startsWith(srcKey)) {
                // Trying to move a folder to a subfolder.
                // Workaround a bug in some implementations of CDMI servers that
                // might return 500 instead of 400.
                return false;
            }
            if (exists(dstKey)) {
                response = connector.moveContainer(srcKey,
                        dstKey + KeyUtils.getBaseName(srcKey) + "/");
            } else {
                response = connector.moveContainer(srcKey, dstKey);
            }
        } else {
            if (srcKey.equals(dstKey)) {
                // Same, nothing to do.
                return true;
            }
            response = connector.moveObject(srcKey, dstKey);
        }
        EntityUtils.consumeQuietly(response.getEntity());
        StatusLine statusLine = response.getStatusLine();
        // FIXME: code should only be SC_CREATED,
        return statusLine.getStatusCode() == HttpStatus.SC_NO_CONTENT
                || statusLine.getStatusCode() == HttpStatus.SC_CREATED;
    }

    @Override
    public boolean makedir(String key) throws IOException {
        if (!key.endsWith("/")) {
            key += "/";
        }
        try {
            FileMetadata status = getMetadata(key);
            if (!status.isContainer()) {
                throw new CdmiConnectionException(
                        "Cannot create a directory over an existing file.");
            }
            return false;
        } catch (FileNotFoundException e) {
            HttpResponse response = connector.createContainer(key);
            EntityUtils.consumeQuietly(response.getEntity());
            StatusLine statusLine = response.getStatusLine();
            // FIXME: code should only be SC_CREATED,
            return statusLine.getStatusCode() == HttpStatus.SC_NO_CONTENT
                    || statusLine.getStatusCode() == HttpStatus.SC_CREATED;
        }
    }

    @Override
    public boolean makedirs(String key) throws IOException {
        if (!key.endsWith("/")) {
            key += "/";
        }
        try {
            FileMetadata status = getMetadata(key);
            if (!status.isContainer()) {
                throw new CdmiConnectionException(
                        "Cannot create a directory over an existing file.");
            }
            return false;
        } catch (FileNotFoundException e) {
            // Normal flow
        }
        File file = new File(key);
        ArrayList<String> allpaths = new ArrayList<String>();
        allpaths.add(file.getPath());
        File parent;
        while ((parent = file.getParentFile()) != null) {
            allpaths.add(parent.getPath());
            file = parent;
        }
        Collections.reverse(allpaths);
        for (String p : allpaths) {
            if (!p.endsWith("/")) {
                p += "/";
            }
            makedir(p); // Don't care about result.
        }
        return true;
    }

    private Iterable<String> getChildren(String key) throws IOException {
        HttpResponse response = connector.listContainer(key);
        return Arrays.asList(parser.extractArray(EntityUtils.toString(response.getEntity()),
                "children"));
    }

    @Override
    public FileMetadata getMetadata(String key) throws IOException {
        // Throws a FileNotFoundException if no such key.
        CdmiMetadata metadata = metadatareader.readMetadata(key);
        return new FileMetadataImpl(metadata);
    }

    @Override
    public FileMetadata[] listMetadata(String key) throws IOException {
        // Throws a FileNotFoundException if no such key.
        CdmiMetadata basemeta = metadatareader.readMetadata(key);
        if (!basemeta.isContainer()) {
            return new FileMetadata[] { new FileMetadataImpl(basemeta) };
        } else {
            // We have a container.
            if (!key.endsWith("/")) {
                key += "/";
            }
            ArrayList<FileMetadata> result = new ArrayList<FileMetadata>();
            for (String child : this.getChildren(key)) {
                String target = key + child;
                try {
                    FileMetadata metadata = getMetadata(target);
                    if (metadata != null) {
                        result.add(metadata);
                    }
                } catch (FileNotFoundException e) {
                    // Continue, the child may have disappeared in between.
                }
            }
            return result.toArray(new FileMetadata[0]);
        }
    }

    @Override
    public String userExtension(String key, String query) throws IOException {
        HttpResponse response = connector.userSpecificQuery(key, query);
        HttpEntity entity = response.getEntity();
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK && entity != null) {
            // If there is an entity, we know the operation succeeded.
            return EntityUtils.toString(entity);
        }
        return null;
    }
}
