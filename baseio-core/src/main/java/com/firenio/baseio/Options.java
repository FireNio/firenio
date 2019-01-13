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
import com.firenio.baseio.component.Native;

/**
 * @author wangkai
 *
 */
public class Options {

    static final String BUF_AUTO_EXPANSION     = "com.firenio.baseio.bufAutoExpansion";
    static final String BUF_RECYCLE            = "com.firenio.baseio.bufRecycle";
    static final String CHANNEL_READ_FIRST     = "com.firenio.baseio.channelReadFirst";
    static final String DEBUG_ERROR_LEVEL      = "com.firenio.baseio.debugErrorLevel";
    static final String ENABLE_EPOLL           = "com.firenio.baseio.ssl.enableEpoll";
    static final String ENABLE_OPENSSL         = "com.firenio.baseio.ssl.enableOpenSsl";
    static final String ENABLE_UNSAFE_BUF      = "com.firenio.baseio.ssl.enableUnsafeBuf";
    static final String OPENSSL_PATH           = "org.wildfly.openssl.path";
    static final String SSL_UNWRAP_BUFFER_SIZE = "com.firenio.baseio.ssl.unwrapBufferSize";

    public static int getDebugErrorLevel() {
        return Util.getIntProperty(DEBUG_ERROR_LEVEL);
    }

    public static String getOpensslPath() {
        return System.getProperty(OPENSSL_PATH);
    }

    public static int getSslUnwrapBufferSize(int defaultValue) {
        return Util.getIntProperty(SSL_UNWRAP_BUFFER_SIZE, defaultValue);
    }

    public static boolean isBufAutoExpansion() {
        return Util.getBooleanProperty(BUF_AUTO_EXPANSION, true);
    }

    public static boolean isBufRecycle() {
        return Util.getBooleanProperty(BUF_RECYCLE);
    }

    public static boolean isChannelReadFirst() {
        return Util.getBooleanProperty(CHANNEL_READ_FIRST);
    }

    public static boolean isEnableEpoll() {
        return Util.getBooleanProperty(ENABLE_EPOLL);
    }

    public static boolean isEnableOpenssl() {
        return Util.getBooleanProperty(ENABLE_OPENSSL);
    }

    public static boolean isEnableUnsafeBuf() {
        return Native.EPOLL_AVAIABLE && Util.getBooleanProperty(ENABLE_UNSAFE_BUF);
    }

    public static void setBufAutoExpansion(boolean auto) {
        System.setProperty(BUF_AUTO_EXPANSION, String.valueOf(auto));
    }

    public static void setBufRecycle(boolean recycle) {
        System.setProperty(BUF_RECYCLE, String.valueOf(recycle));
    }

    public static void setChannelReadFirst(boolean channelReadFirst) {
        System.setProperty(CHANNEL_READ_FIRST, String.valueOf(channelReadFirst));
    }

    public static void setDebugErrorLevel(int level) {
        System.setProperty(DEBUG_ERROR_LEVEL, String.valueOf(level));
    }

    public static void setEnableEpoll(boolean enable) {
        System.setProperty(ENABLE_EPOLL, String.valueOf(enable));
    }

    public static void setEnableOpenssl(boolean enable) {
        System.setProperty(ENABLE_OPENSSL, String.valueOf(enable));
    }

    public static void setEnableUnsafeBuf(boolean enable) {
        System.setProperty(ENABLE_UNSAFE_BUF, String.valueOf(enable));
    }

    public static void setOpensslPath(String path) {
        System.setProperty(OPENSSL_PATH, path);
    }

    public static void setSslUnwrapBufferSize(int size) {
        System.setProperty(SSL_UNWRAP_BUFFER_SIZE, String.valueOf(size));
    }

}
