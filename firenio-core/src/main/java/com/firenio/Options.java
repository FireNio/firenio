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

/**
 * @author wangkai
 */
public class Options {

    static final String BUF_AUTO_EXPANSION     = "com.firenio.buf_auto_expansion";
    static final String BUF_FAST_INDEX_OF      = "com.firenio.buf_auto_expansion";
    static final String BUF_THREAD_YIELD       = "com.firenio.buf_thread_yield";
    static final String CHANNEL_READ_FIRST     = "com.firenio.channel_read_first";
    static final String ENABLE_UNSAFE          = "com.firenio.enable_unsafe";
    static final String ENABLE_EPOLL           = "com.firenio.ssl.enable_epoll";
    static final String ENABLE_OPENSSL         = "com.firenio.ssl.enable_openssl";
    static final String ENABLE_UNSAFE_BUF      = "com.firenio.ssl.enable_unsafe_buf";
    static final String OPENSSL_PATH           = "org.wildfly.openssl.path";
    static final String SSL_UNWRAP_BUFFER_SIZE = "com.firenio.ssl.unwrap_buffer_size";
    static final String SYS_CLOCK_STEP         = "com.firenio.sys_clock_step";

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

    public static boolean isBufFastIndexOf() {
        return isTrue(BUF_AUTO_EXPANSION, false);
    }

    public static boolean isBufThreadYield() {
        return isTrue(BUF_THREAD_YIELD, false);
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
        return isTrue(ENABLE_UNSAFE_BUF);
    }

    public static void setBufAutoExpansion(boolean auto) {
        System.setProperty(BUF_AUTO_EXPANSION, String.valueOf(auto));
    }

    public static void setBufFastIndexOf(boolean fast) {
        System.setProperty(BUF_AUTO_EXPANSION, String.valueOf(fast));
    }

    public static void setBufThreadYield(boolean yield) {
        setBool(BUF_THREAD_YIELD, yield);
    }

    public static void setChannelReadFirst(boolean channelReadFirst) {
        setBool(CHANNEL_READ_FIRST, channelReadFirst);
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
