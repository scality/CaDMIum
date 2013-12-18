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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import com.scality.cdmi.api.CdmiClient;
import com.scality.cdmi.api.CdmiConnectionException;
import com.scality.cdmi.api.FileMetadata;
import com.scality.cdmi.connector.CdmiInputStream;
import com.scality.cdmi.connector.CdmiOutputStream;
import com.scality.cdmi.impl.FileMetadataImpl;
import com.scality.cdmi.impl.utils.KeyUtils;

/**
 * A mock CDMI client that can be used for testing. It uses the local filesystem
 * underneath.
 * 
 * @author ziad.bizri@ezako.com for Scality
 */
public class MockCdmiClient implements CdmiClient {
    /**
     * A map of all the remote file, each entry in the map points to a
     * {@link File} stored in the local filesystem.
     */
    private TreeMap<String, File> remoteFiles;
    /**
     * A map of all the remote containers. These are not mapped to the local
     * filesystem.
     */
    private TreeSet<String> remoteDirs;

    public MockCdmiClient() {
        remoteFiles = new TreeMap<String, File>();
        remoteDirs = new TreeSet<String>();
        remoteDirs.add("/");
    }
    
    private String getContainerKey(String key) {
        if (key.length() > 1 && key.endsWith("/")) {
            return new String(key.substring(0, key.length() - 1));
        }
        return key;
    }
    
    private String getContainerName(String key) {
        if (!key.endsWith("/")) {
            return key + "/";
        }
        return key;
    }

    @Override
    public boolean put(String key, File file) throws IOException {
        if (remoteFiles.containsKey(key)) {
            return false;
        }
        // Open the stream to check for file errors.
        FileInputStream is = new FileInputStream(file.getPath());
        is.close();
        remoteFiles.put(key, file);
        return true;
    }

    @Override
    public boolean get(String key, File destFile) throws IOException {
        if (!exists(key)) {
            return false;
        }
        FileOutputStream out = new FileOutputStream(destFile.getPath());
        FileInputStream is = new FileInputStream(remoteFiles.get(key));
        byte[] buff = new byte[1024];
        int read;
        while ((read = is.read(buff)) > -1) {
            out.write(buff, 0, read);
        }
        is.close();
        out.close();
        return true;
    }

    @Override
    public boolean touch(String key) throws IOException {
        if (exists(key)) {
            return false;
        }
        File file = File.createTempFile(new File(key).getName(), ".local");
        FileOutputStream out = new FileOutputStream(file);
        out.close();
        remoteFiles.put(key, file);
        return true;
    }

    @Override
    public boolean exists(String key) throws IOException {
        return remoteDirs.contains(getContainerKey(key))
                || remoteFiles.containsKey(key);
    }

    @Override
    public CdmiInputStream open(String key) throws IOException {
        if (remoteFiles.containsKey(key)) {
            return new MockCdmiInputStream(new FileInputStream(
                    remoteFiles.get(key)), -1);
        }
        throw new FileNotFoundException();
    }

    @Override
    public CdmiInputStream open(String key, long startPos) throws IOException {
        return open(key, startPos, -1);
    }

    @Override
    public CdmiInputStream open(String key, long startPos, int maxRead)
            throws IOException {
        CdmiInputStream is = new MockCdmiInputStream(new FileInputStream(
                remoteFiles.get(key)), maxRead);
        is.skip(startPos);
        return is;
    }

    @Override
    public CdmiOutputStream write(String key, long startPos) throws IOException {
        if (!remoteFiles.containsKey(key)) {
            touch(key);
        }
        MockCdmiOutputStream os = new MockCdmiOutputStream(remoteFiles.get(key));
        os.setPos(startPos);
        return os;
    }

    @Override
    public CdmiOutputStream append(String key) throws IOException {
        if (!remoteFiles.containsKey(key)) {
            touch(key);
        }
        File outputFile = remoteFiles.get(key);
        return this.write(key, outputFile.length());
    }

    @Override
    public boolean delete(String key, boolean recursive) throws IOException {
        if (remoteFiles.containsKey(key)) {
            remoteFiles.remove(key);
            return true;
        } else {
            key = getContainerKey(key);
            if (remoteDirs.contains(key)) {
                ArrayList<String> dirstodelete = new ArrayList<String>();
                for (String dir : remoteDirs) {
                    if (dir.startsWith(key)) {
                        dirstodelete.add(dir);
                        if (!dir.equals(key) && !recursive) {
                            throw new IOException();
                        }
                    }
                }
                ArrayList<String> filestodelete = new ArrayList<String>();
                for (String f : remoteFiles.keySet()) {
                    if (f.startsWith(key)) {
                        filestodelete.add(f);
                        if (!recursive) {
                            throw new IOException();
                        }
                    }
                }
                remoteDirs.removeAll(dirstodelete);
                for (String f : filestodelete) {
                    remoteFiles.remove(f);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean move(String srcKey, String dstKey) throws IOException {
        // Remove trailing slashes if any.
        srcKey = getContainerKey(srcKey);
        if (remoteFiles.containsKey(srcKey)) {
            String destination = dstKey;
            // Move to a directory.
            if (dstKey.endsWith("/")) {
                destination = dstKey + KeyUtils.getBaseName(srcKey);
            } else if (remoteDirs.contains(dstKey)) {
                destination = dstKey + "/" + KeyUtils.getBaseName(srcKey);
            }
            if (!remoteDirs.contains(KeyUtils.getParentContainerName(destination))) {
                return false;
            }
            remoteFiles.put(destination, remoteFiles.get(srcKey));
            remoteFiles.remove(srcKey);
            return true;
        } else if (remoteDirs.contains(srcKey)) {
            if (remoteFiles.containsKey(getContainerKey(dstKey)) ||
                    srcKey.equals(getContainerKey(dstKey))) {
                return false;
            }
            String destination = dstKey;
            // Move to a directory.
            if (dstKey.endsWith("/")) {
                destination = dstKey + KeyUtils.getBaseName(srcKey);
            } else if (remoteDirs.contains(dstKey)) {
                destination = dstKey + "/" + KeyUtils.getBaseName(srcKey);
            }
            if (!remoteDirs.contains(KeyUtils.getParentContainerName(destination))) {
                return false;
            }                
            remoteDirs.add(destination);
            remoteDirs.remove(srcKey);
            ArrayList<String> filesForMove = new ArrayList<String>();
            for (String filename : remoteFiles.keySet()) {
                if (filename.startsWith(srcKey)) {
                    filesForMove.add(filename);
                }
            }
            for (String filename : filesForMove) {
                String destfilename = filename.replace(srcKey, destination);
                remoteFiles.put(destfilename, remoteFiles.get(filename));
                remoteFiles.remove(filename);
            }
            return true;
        } else {
            throw new FileNotFoundException(srcKey);
        }
    }

    @Override
    public boolean makedir(String key) throws IOException {
        key = getContainerKey(key);
        if (exists(key)) {
            return false;
        }
        String basedir = new File(key).getParent();
        if (basedir == null || !remoteDirs.contains(basedir)) {
            return false;
        }
        remoteDirs.add(key);
        return true;
    }

    @Override
    public boolean makedirs(String key) throws IOException {
        key = getContainerKey(key);
        if (exists(key)) {
            return false;
        }
        String[] tokens = key.split("/");
        String accumulator = "";
        for (String token : tokens) {
            if (!token.isEmpty()) {
                accumulator += "/" + token;
                if (remoteFiles.containsKey(accumulator)) {
                    throw new CdmiConnectionException();
                } else {
                    remoteDirs.add(accumulator);
                }
            }
        }
        return true;
    }

    @Override
    public FileMetadata getMetadata(String key) throws IOException {
        if (remoteFiles.containsKey(key)) {
            return new FileMetadataImpl(key, remoteFiles.get(key).length(),
                    100, 100, false, "{}");
        } else if (remoteDirs.contains(getContainerKey(key))) {
            return new FileMetadataImpl(getContainerName(key), -1, 100, 100,
                    true, "{}");
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public FileMetadata[] listMetadata(String key) throws IOException {
        ArrayList<FileMetadata> result = new ArrayList<FileMetadata>();
        if (remoteFiles.containsKey(key)) {
            result.add(new FileMetadataImpl(key, remoteFiles.get(key).length(),
                    100, 100, false, "{}"));
        } else {
            key = getContainerKey(key);
            if (remoteDirs.contains(key)) {
                for (String filename : remoteFiles.keySet()) {
                    if (key.equals(KeyUtils.getParentContainerName(filename))) {
                        result.add(new FileMetadataImpl(filename, remoteFiles
                                .get(filename).length(), 100, 100, false,
                                "{}"));
                    }
                }
                for (String dirname : remoteDirs) {
                    if (!dirname.equals(key) &&
                            key.equals(KeyUtils.getParentContainerName(dirname))) {
                        result.add(new FileMetadataImpl(
                                getContainerName(dirname), -1, 100, 100, true,
                                "{}"));
                    }
                }
            } else {
                throw new FileNotFoundException(key);
            }
        }
        return result.toArray(new FileMetadata[0]);
    }

    @Override
    public String userExtension(String key, String query) throws IOException {
        // No support for extensions.
        return null;
    }
}
