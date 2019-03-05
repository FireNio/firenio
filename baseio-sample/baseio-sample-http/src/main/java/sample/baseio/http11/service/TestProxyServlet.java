/*
 * Copyright 2015 The Baseio Project
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
package sample.baseio.http11.service;

import java.util.Iterator;

import org.springframework.stereotype.Service;

import com.firenio.baseio.buffer.PooledByteBufAllocator;
import com.firenio.baseio.buffer.PooledByteBufAllocator.BufDebug;
import com.firenio.baseio.codec.http11.HttpFrame;
import com.firenio.baseio.component.Channel;

import sample.baseio.http11.HttpFrameAcceptor;

@Service("/bufDebug")
public class TestProxyServlet extends HttpFrameAcceptor {

    @Override
    protected void doAccept(Channel ch, HttpFrame frame) throws Exception {
        synchronized (PooledByteBufAllocator.BUF_DEBUGS) {
            Iterator<BufDebug> it = PooledByteBufAllocator.BUF_DEBUGS.values().iterator();
            if (it.hasNext()) {
                BufDebug d = it.next();
                if (d != null) {
                    Exception e = d.e;
                    if (e != null) {
                        throw e;
                    }
                }
            }
        }
        frame.setString("not found");
        ch.writeAndFlush(frame);
    }

}
