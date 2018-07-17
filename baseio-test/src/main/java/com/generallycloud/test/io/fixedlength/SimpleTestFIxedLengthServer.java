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
package com.generallycloud.test.io.fixedlength;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Future;

public class SimpleTestFIxedLengthServer {

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                FixedLengthFuture f = (FixedLengthFuture) future;
                future.write("yes server already accept your message:", channel.getCharset());
                future.write(f.getReadText(), channel.getCharset());
                channel.flush(future);
            }
        };
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelContext context = new ChannelContext(8300);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new FixedLengthCodec());
        acceptor.bind();
    }

}
