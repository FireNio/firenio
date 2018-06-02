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

import com.generallycloud.baseio.Releasable;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class ReleaseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseUtil.class);

    public static void release(Releasable releasable, long version) {
        if (releasable == null) {
            return;
        }
        try {
            releasable.release(version);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void release(Releasable releasable) {
        if (releasable == null) {
            return;
        }
        try {
            releasable.release(releasable.getReleaseVersion());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void release(ChannelFuture future, NioEventLoop eventLoop) {
        if (future == null) {
            return;
        }
        try {
            future.release(eventLoop);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

}
