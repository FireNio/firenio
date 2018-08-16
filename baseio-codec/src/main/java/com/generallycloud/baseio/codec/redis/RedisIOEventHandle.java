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
package com.generallycloud.baseio.codec.redis;

import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.protocol.Frame;

public class RedisIOEventHandle extends IoEventHandle {

    private Waiter waiter;

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        RedisFrame f = (RedisFrame) frame;
        Waiter waiter = this.waiter;
        if (waiter != null) {
            this.waiter = null;
            waiter.response(f);
        }
    }

    public Waiter newWaiter() {
        this.waiter = new Waiter();
        return this.waiter;
    }

}
