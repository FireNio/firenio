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

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.protocol.Future;

public class WaiterOnFuture implements OnFuture {

    private Waiter<Future> waiter = new Waiter<>();

    /**
     * @param timeout
     * @return timeouted
     */
    public boolean await(long timeout) {
        return waiter.await(timeout);
    }

    public Future getReadFuture() {
        return waiter.getPayload();
    }

    @Override
    public void onResponse(SocketSession session, Future future) {
        this.waiter.setPayload(future);
    }
}
