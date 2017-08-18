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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;

/**
 * @author wangkai
 *
 */
public class DatagramSelectorEventLoop extends AbstractSelectorLoop {

    private DatagramChannelContext         context;
    private DatagramSelectorEventLoopGroup eventLoopGroup;
    private DatagramChannel                channel;
    private Selector                       selector;
    private ByteBufAllocator               allocator;
    private DatagramSessionManager         sessionManager;
    private Logger                         logger = LoggerFactory.getLogger(getClass());

    public DatagramSelectorEventLoop(DatagramSelectorEventLoopGroup group, int coreIndex,
            DatagramChannel channel) {
        super(group.getChannelContext(), coreIndex);
        this.eventLoopGroup = group;
        this.context = group.getChannelContext();
        this.channel = channel;
        this.sessionManager = context.getSessionManager();
        this.allocator = UnpooledByteBufAllocator.getHeapInstance();
    }

    private void accept(SelectionKey selectionKey) {

        try {

            DatagramChannelContext context = this.context;

            //FIXME 使用 ByteBuffer
            ByteBuf buf = allocator.allocate(DatagramPacket.PACKET_MAX);

            DatagramChannel channel = (DatagramChannel) selectionKey.channel();

            InetSocketAddress remoteAddress = (InetSocketAddress) channel.receive(buf.nioBuffer());

            DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

            DatagramPacket packet = DatagramPacket.createPacket(buf.reverse().flip());

            DatagramSession session = sessionManager.getSession(channel, remoteAddress, this);

            acceptor.accept(session, packet);

        } catch (Throwable e) {

            cancelSelectionKey(selectionKey, e);
        }
    }

    private void cancelSelectionKey(SelectionKey selectionKey, Throwable e) {

        Object attachment = selectionKey.attachment();

        if (attachment instanceof Channel) {

            CloseUtil.close((Channel) attachment);
        }

        logger.error(e.getMessage(), e);
    }

    @Override
    protected void doLoop() throws IOException {

        Selector selector = this.selector;

        int selected = selector.select(16);

        if (selected < 1) {
            return;
        }

        Set<SelectionKey> sks = selector.selectedKeys();

        for (SelectionKey key : sks) {

            if (!key.isValid()) {
                continue;
            }

            accept(key);
        }

        sks.clear();
    }

    @Override
    public DatagramChannelContext getChannelContext() {
        return context;
    }

    @Override
    public DatagramSelectorEventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public void rebuildSelector() throws IOException {
        // 打开selector
        this.selector = Selector.open();
        // 注册监听事件到该selector
        this.channel.register(selector, SelectionKey.OP_READ);
    }

}
