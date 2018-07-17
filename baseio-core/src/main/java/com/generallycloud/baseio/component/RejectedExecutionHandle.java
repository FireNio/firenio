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
package com.generallycloud.baseio.component;

import java.io.Closeable;
import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.EventLoop;

/**
 * @author wangkai
 *
 */
public interface RejectedExecutionHandle {

    void reject(EventLoop eventLoop, Runnable runnable);

    public class DefaultRejectedExecutionHandle implements RejectedExecutionHandle {

        @Override
        public void reject(EventLoop eventLoop, Runnable runnable) {
            if (runnable instanceof Closeable) {
                CloseUtil.close((Closeable) runnable);
            } else {
                throw new RejectedExecutionException();
            }
        }
    }
    
}
