/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firenio.baseio;

import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public class Options {

    static final String BYTEBUF_DEBUG          = "com.firenio.baseio.bytebufDebug";
    static final String BYTEBUF_RECYCLE        = "com.firenio.baseio.bytebufRecycle";
    static final String CHANNEL_READ_FIRST     = "com.firenio.baseio.channelReadFirst";
    static final String DEBUG_ERROR_LEVEL      = "com.firenio.baseio.DebugErrorLevel";
    static final String DISABLE_OPENSSL        = "com.firenio.baseio.ssl.disableOpenSsl";
    static final String OPENSSL_PATH           = "org.wildfly.openssl.path";
    static final String SSL_UNWRAP_BUFFER_SIZE = "com.firenio.baseio.ssl.unwrapBufferSize";

    public static int getDebugErrorLevel() {
        return Util.getProperty(DEBUG_ERROR_LEVEL);
    }

    public static String getOpensslPath() {
        return System.getProperty(OPENSSL_PATH);
    }

    public static int getSslUnwrapBufferSize(int defaultValue) {
        return Util.getProperty(SSL_UNWRAP_BUFFER_SIZE, defaultValue);
    }

    public static boolean isByteBufDebug() {
        return Util.isSystemTrue(BYTEBUF_DEBUG);
    }

    public static boolean isBytebufRecycle() {
        return Util.isSystemTrue(BYTEBUF_RECYCLE);
    }

    public static boolean isChannelReadFirst() {
        return Util.isSystemTrue(CHANNEL_READ_FIRST);
    }

    public static boolean isDisableOpenssl() {
        return Util.isSystemTrue(DISABLE_OPENSSL);
    }

    public static void setByteBufDebug(boolean debug) {
        System.setProperty(BYTEBUF_DEBUG, String.valueOf(debug));
    }

    public static void setBytebufRecycle(boolean recycle) {
        System.setProperty(BYTEBUF_RECYCLE, String.valueOf(recycle));
    }

    public static void setChannelReadFirst(boolean channelReadFirst) {
        System.setProperty(CHANNEL_READ_FIRST, String.valueOf(channelReadFirst));
    }

    public static void setDebugErrorLevel(int level) {
        System.setProperty(DEBUG_ERROR_LEVEL, String.valueOf(level));
    }

    public static void setDisableOpenssl(boolean disable) {
        System.setProperty(DISABLE_OPENSSL, String.valueOf(disable));
    }

    public static void setOpensslPath(String path) {
        System.setProperty(OPENSSL_PATH, path);
    }

    public static void setSslUnwrapBufferSize(int size) {
        System.setProperty(SSL_UNWRAP_BUFFER_SIZE, String.valueOf(size));
    }

}
