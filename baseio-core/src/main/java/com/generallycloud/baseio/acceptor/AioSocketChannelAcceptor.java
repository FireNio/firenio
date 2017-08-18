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
package com.generallycloud.baseio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.AioSocketChannel;
import com.generallycloud.baseio.component.AioSocketChannelContext;
import com.generallycloud.baseio.component.CachedAioThread;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class AioSocketChannelAcceptor extends AbstractSocketChannelAcceptor {

    private AsynchronousServerSocketChannel serverSocketChannel;

    public AioSocketChannelAcceptor(AioSocketChannelContext context) {
        super(context);
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void bind(InetSocketAddress socketAddress) throws IOException {

        AioSocketChannelContext context = (AioSocketChannelContext) getContext();

        AsynchronousChannelGroup group = context.getAsynchronousChannelGroup();

        final FixedAtomicInteger channelIds = new FixedAtomicInteger(1);

        serverSocketChannel = AsynchronousServerSocketChannel.open(group);

        serverSocketChannel.bind(socketAddress);

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            @Override
            public void completed(AsynchronousSocketChannel _channel, Void attachment) {

                serverSocketChannel.accept(null, this); // 接受下一个连接

                int channelId = channelIds.getAndIncrement();

                CachedAioThread aioThread = (CachedAioThread) Thread.currentThread();

                AioSocketChannel channel = new AioSocketChannel(aioThread, _channel, channelId);

                channel.fireOpend();

                aioThread.getReadCompletionHandler().completed(0, channel);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                logger.error(exc.getMessage(), exc);
            }
        });

        logger.info("22222222222222222");
    }

    @Override
    protected void destroyService() {
        CloseUtil.close(serverSocketChannel);
    }
}
