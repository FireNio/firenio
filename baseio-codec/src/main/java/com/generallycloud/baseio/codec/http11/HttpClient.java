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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.Waiter;

public class HttpClient {

    private NioSocketChannel  ch;
    private HttpIOEventHandle ioEventHandle;

    public HttpClient(NioSocketChannel ch) {
        this.ch = ch;
        this.ioEventHandle = (HttpIOEventHandle) ch.getIoEventHandle();
    }

    public synchronized HttpFrame request(HttpFrame frame, long timeout) throws IOException {
        Waiter waiter = ioEventHandle.newWaiter();
        ch.flush(frame);
        if (waiter.await(timeout)) {
            throw new TimeoutException("timeout");
        }
        return (HttpFrame) waiter.getResponse();
    }

    public synchronized HttpFrame request(HttpFrame frame) throws IOException {
        return request(frame, 3000);
    }

}
