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
package com.generallycloud.baseio.container.protobase;

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.protocol.Frame;

public class WaiterOnFrame implements OnFrame {

    private Waiter waiter = new Waiter();

    /**
     * @param timeout
     * @return timeouted
     */
    public boolean await(long timeout) {
        return waiter.await(timeout);
    }

    public Frame getReadFrame() {
        return (Frame) waiter.getResponse();
    }

    @Override
    public void onResponse(NioSocketChannel ch, Frame frame) {
        this.waiter.response(frame);
    }
}
