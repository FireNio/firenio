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
package com.generallycloud.baseio.concurrent;

import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.component.RejectedExecutionHandle;

public interface EventLoopGroup extends LifeCycle {

    EventLoop getEventLoop(int index);

    EventLoopListener getEventLoopListener();

    EventLoop getNext();

    RejectedExecutionHandle getRejectedExecutionHandle();

    void setEventLoopListener(EventLoopListener listener);

    void setRejectedExecutionHandle(RejectedExecutionHandle rejectedExecutionHandle);

}
