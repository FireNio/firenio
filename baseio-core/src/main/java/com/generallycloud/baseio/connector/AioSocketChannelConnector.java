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
package com.generallycloud.baseio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.generallycloud.baseio.component.AioSocketChannel;
import com.generallycloud.baseio.component.AioSocketChannelContext;
import com.generallycloud.baseio.component.CachedAioThread;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class AioSocketChannelConnector extends AbstractSocketChannelConnector {

    private AioSocketChannelContext context;

    public AioSocketChannelConnector(AioSocketChannelContext context) {
        this.context = context;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void connect(InetSocketAddress socketAddress) throws IOException {

        AsynchronousChannelGroup group = context.getAsynchronousChannelGroup();

        final AsynchronousSocketChannel _channel = AsynchronousSocketChannel.open(group);

        _channel.connect(socketAddress, this,
                new CompletionHandler<Void, AioSocketChannelConnector>() {

                    @Override
                    public void completed(Void result, AioSocketChannelConnector connector) {

                        CachedAioThread aioThread = (CachedAioThread) Thread.currentThread();

                        AioSocketChannel channel = new AioSocketChannel(aioThread, _channel, 1);

                        connector.finishConnect(channel.getSession(), null);

                        aioThread.getReadCompletionHandler().completed(0, channel);
                    }

                    @Override
                    public void failed(Throwable exc, AioSocketChannelConnector connector) {
                        connector.finishConnect(session, exc);
                    }
                });

        wait4connect();
    }

    @Override
    public AioSocketChannelContext getContext() {
        return context;
    }

    @Override
    protected void destroyService() {}

    @Override
    Logger getLogger() {
        return logger;
    }

}
