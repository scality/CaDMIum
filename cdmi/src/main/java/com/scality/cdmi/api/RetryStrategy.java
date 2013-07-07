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

/**
 * Simple bean used in storing the retry strategy between requests to the CDMI
 * server.
 * 
 * @author ziad.bizri@ezako.com for Scality
 * 
 */
public class RetryStrategy {
    int maxRetries;
    int sleepTimeBetweenRetriesMillis;
    int timeOutMillis;

    /**
     * Default constructor with timeout at 10seconds and no sleep between retries
     */
    public RetryStrategy() {
        maxRetries = 0;
        sleepTimeBetweenRetriesMillis = 0;
        timeOutMillis = 10000;
    }

    /**
     * Constructor 
     * @param maxRetries
     * @param sleepTimeBetweenRetries
     * @param timeOutMillis timeout in milliseconds
     */
    public RetryStrategy(int maxRetries, int sleepTimeBetweenRetries, int timeOutMillis) {
        this.maxRetries = maxRetries;
        this.sleepTimeBetweenRetriesMillis = sleepTimeBetweenRetries;
        this.timeOutMillis = timeOutMillis;
    }

    /**
     * @return
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * @return
     */
    public int getSleepTimeBetweenRetries() {
        return sleepTimeBetweenRetriesMillis;
    }

    /**
     * @return
     */
    public int getTimeOutMillis() {
        return timeOutMillis;
    }

    @Override
    public String toString() {
        return "RetryStrategy [maxRetries=" + maxRetries + ", sleepTimeBetweenRetries="
                + sleepTimeBetweenRetriesMillis + ", timeout=" + timeOutMillis + "]";
    }
}
