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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public class OnFrameWrapper implements OnFrame {

    private OnFrame                      listener = null;

    private BlockingQueue<WaiterOnFrame> waiters  = new ArrayBlockingQueue<>(1024 * 8);

    @Override
    public void onResponse(final NioSocketChannel ch, final Frame frame) {

        WaiterOnFrame waiter = waiters.poll();

        if (waiter != null) {

            waiter.onResponse(ch, frame);

            return;
        }

        if (listener == null) {
            return;
        }

        listener.onResponse(ch, frame);
    }

    public void listen(WaiterOnFrame onReadFrame) {
        this.waiters.offer(onReadFrame);
    }

    public OnFrame getListener() {
        return listener;
    }

    public void setListener(OnFrame listener) {
        this.listener = listener;
    }
}
