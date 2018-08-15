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
package com.generallycloud.baseio.common;

import java.io.Closeable;
import java.nio.channels.Selector;

import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class CloseUtil {

    private static Logger logger = LoggerFactory.getLogger(CloseUtil.class);

    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void close(Selector selector) {
        if (selector == null) {
            return;
        }
        try {
            selector.close();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public static void unbind(ChannelAcceptor unbindable) {
        if (unbindable == null) {
            return;
        }
        try {
            unbindable.unbind();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
