/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio;

import com.generallycloud.baseio.common.PropertiesUtil;

/**
 * @author wangkai
 *
 */
public class Options {

    static final String DEVELOP_DEBUG          = "com.generallycloud.baseio.develop.debug";
    static final String DISABLE_OPENSSL        = "com.generallycloud.baseio.ssl.disableOpenSsl";
    static final String OPENSSL_PATH           = "org.wildfly.openssl.path";
    static final String SSL_UNWRAP_BUFFER_SIZE = "com.generallycloud.baseio.ssl.unwrapBufferSize";
    static final String CHANNEL_READ_FIRST     = "com.generallycloud.baseio.channelReadFirst";

    public static String getOpensslPath() {
        return System.getProperty(OPENSSL_PATH);
    }

    public static int getSslUnwrapBufferSize(int defaultValue) {
        return PropertiesUtil.getProperty(SSL_UNWRAP_BUFFER_SIZE, defaultValue);
    }

    public static boolean isDevelopDebug() {
        return PropertiesUtil.isSystemTrue(DEVELOP_DEBUG);
    }

    public static boolean isChannelReadFirst() {
        return PropertiesUtil.isSystemTrue(CHANNEL_READ_FIRST);
    }

    public static boolean isDisableOpenssl() {
        return PropertiesUtil.isSystemTrue(DISABLE_OPENSSL);
    }

    public static void setDevelopDebug(boolean debug) {
        System.setProperty(DEVELOP_DEBUG, String.valueOf(debug));
    }

    public static void setChannelReadFirst(boolean channelReadFirst) {
        System.setProperty(CHANNEL_READ_FIRST, String.valueOf(channelReadFirst));
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
