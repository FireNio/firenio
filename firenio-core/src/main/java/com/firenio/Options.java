/*
 * Copyright 2015 The FireNio Project
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
package com.firenio;

import com.firenio.common.Util;
import com.firenio.component.Native;

/**
 * @author wangkai
 */
public class Options {

    static final String BUF_AUTO_EXPANSION     = "com.firenio.bufAutoExpansion";
    static final String BUF_THREAD_YIELD       = "com.firenio.bufThreadYield";
    static final String BUF_RECYCLE            = "com.firenio.bufRecycle";
    static final String CHANNEL_READ_FIRST     = "com.firenio.channelReadFirst";
    static final String DEBUG_ERROR            = "com.firenio.debugError";
    static final String ENABLE_UNSAFE          = "com.firenio.enableUnsafe";
    static final String ENABLE_EPOLL           = "com.firenio.ssl.enableEpoll";
    static final String ENABLE_OPENSSL         = "com.firenio.ssl.enableOpenSsl";
    static final String ENABLE_UNSAFE_BUF      = "com.firenio.ssl.enableUnsafeBuf";
    static final String OPENSSL_PATH           = "org.wildfly.openssl.path";
    static final String SSL_UNWRAP_BUFFER_SIZE = "com.firenio.ssl.unwrapBufferSize";
    static final String SYS_CLOCK_STEP         = "com.firenio.sysClockStep";

    public static boolean isDebugError() {
        return isTrue(DEBUG_ERROR);
    }

    public static String getOpensslPath() {
        return System.getProperty(OPENSSL_PATH);
    }

    public static int getSslUnwrapBufferSize(int defaultValue) {
        return getInt(SSL_UNWRAP_BUFFER_SIZE, defaultValue);
    }

    public static int getSysClockStep() {
        return getInt(SYS_CLOCK_STEP);
    }

    public static boolean isBufAutoExpansion() {
        return isTrue(BUF_AUTO_EXPANSION, true);
    }

    public static boolean isBufThreadYield() {
        return isTrue(BUF_THREAD_YIELD, false);
    }

    public static boolean isBufRecycle() {
        return isTrue(BUF_RECYCLE);
    }

    public static boolean isChannelReadFirst() {
        return isTrue(CHANNEL_READ_FIRST);
    }

    public static boolean isEnableUnsafe() {
        return isTrue(ENABLE_UNSAFE, true);
    }

    public static boolean isEnableEpoll() {
        return isTrue(ENABLE_EPOLL, true);
    }

    public static boolean isEnableOpenssl() {
        return isTrue(ENABLE_OPENSSL);
    }

    public static boolean isEnableUnsafeBuf() {
        return Native.EPOLL_AVAILABLE && isTrue(ENABLE_UNSAFE_BUF);
    }

    public static void setBufAutoExpansion(boolean auto) {
        System.setProperty(BUF_AUTO_EXPANSION, String.valueOf(auto));
    }

    public static void setBufThreadYield(boolean yield) {
        setBool(BUF_THREAD_YIELD, yield);
    }

    public static void setBufRecycle(boolean recycle) {
        setBool(BUF_RECYCLE, recycle);
    }

    public static void setChannelReadFirst(boolean channelReadFirst) {
        setBool(CHANNEL_READ_FIRST, channelReadFirst);
    }

    public static void setDebugError(boolean debugError) {
        setBool(DEBUG_ERROR, debugError);
    }

    public static void setEnableUnsafe(boolean enable) {
        setBool(ENABLE_UNSAFE, enable);
    }

    public static void setEnableEpoll(boolean enable) {
        setBool(ENABLE_EPOLL, enable);
    }

    public static void setEnableOpenssl(boolean enable) {
        setBool(ENABLE_OPENSSL, enable);
    }

    public static void setEnableUnsafeBuf(boolean enable) {
        setBool(ENABLE_UNSAFE_BUF, enable);
    }

    public static void setOpensslPath(String path) {
        System.setProperty(OPENSSL_PATH, path);
    }

    public static void setSslUnwrapBufferSize(int size) {
        setInt(SSL_UNWRAP_BUFFER_SIZE, size);
    }

    public static void setSysClockStep(int step) {
        setInt(SYS_CLOCK_STEP, step);
    }

    private static boolean isTrue(String key) {
        return isTrue(key, false);
    }

    private static boolean isTrue(String key, boolean def) {
        String v = Util.getStringProperty(key);
        if (Util.isNullOrBlank(v)) {
            return def;
        }
        return "1".equals(v) || "true".equals(v);
    }

    private static void setBool(String key) {
        setBool(key, false);
    }

    private static void setBool(String key, boolean value) {
        setInt(key, value ? 1 : 0);
    }

    private static int getInt(String key) {
        return Util.getIntProperty(key);
    }

    private static int getInt(String key, int def) {
        return Util.getIntProperty(key, def);
    }

    private static void setInt(String key, int value) {
        System.setProperty(key, String.valueOf(value));
    }

}
