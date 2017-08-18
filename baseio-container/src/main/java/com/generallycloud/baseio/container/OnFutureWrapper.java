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
package com.generallycloud.baseio.container;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.Future;

public class OnFutureWrapper implements OnFuture {

    private OnFuture                      listener = null;

    private BlockingQueue<WaiterOnFuture> waiters  = new ArrayBlockingQueue<>(
            1024 * 8);

    @Override
    public void onResponse(final SocketSession session, final Future future) {

        WaiterOnFuture waiter = waiters.poll();

        if (waiter != null) {

            waiter.onResponse(session, future);

            return;
        }

        if (listener == null) {
            return;
        }

        listener.onResponse(session, future);
    }

    public void listen(WaiterOnFuture onReadFuture) {
        this.waiters.offer(onReadFuture);
    }

    public OnFuture getListener() {
        return listener;
    }

    public void setListener(OnFuture listener) {
        this.listener = listener;
    }
}
