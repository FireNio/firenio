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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.protocol.DatagramPacket;

public class NioDatagramChannel extends AbstractChannel
        implements com.generallycloud.baseio.component.DatagramChannel {

    private DatagramChannel           channel;
    private DatagramChannelContext    context;
    private DatagramSession           session;
    private DatagramSelectorEventLoop selectorLoop;

    public NioDatagramChannel(DatagramSelectorEventLoop selectorLoop, DatagramChannel channel,
            InetSocketAddress remote, int channelId) {
        super(selectorLoop.getByteBufAllocator(), selectorLoop.getChannelContext(), channelId);
        this.selectorLoop = selectorLoop;
        this.context = selectorLoop.getChannelContext();
        this.channel = channel;
        this.remote = remote;
        this.session = new DatagramSession(this);
    }

    @Override
    public void close() throws IOException {
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            if (!isOpened()) {
                return;
            }
            physicalClose();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public DatagramChannelContext getContext() {
        return context;
    }

    @Override
    protected void physicalClose() {
        CloseUtil.close(channel);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        if (local == null) {
            try {
                local = (InetSocketAddress) channel.getLocalAddress();
            } catch (IOException e) {
                local = ERROR_SOCKET_ADDRESS;
            }
        }
        return local;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return remote;
    }

    @Override
    public DatagramSession getSession() {
        return session;
    }

    //	private void sendPacket(ByteBuf buf, SocketAddress socketAddress) throws IOException {
    //		channel.send(buf.nioBuffer(), socketAddress);
    //	}

    private void sendPacket(ByteBuffer buffer, SocketAddress socketAddress) throws IOException {
        channel.send(buffer, socketAddress);
    }

    @Override
    protected String getMarkPrefix() {
        return "udp";
    }

    @Override
    public boolean isOpened() {
        return channel.isConnected() || channel.isOpen();
    }

    @Override
    public void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException {
        sendPacket(ByteBuffer.wrap(packet.getData()), socketAddress);
        //		ByteBuf buf = allocate(packet);
        //		try {
        //			sendPacket(buf.flip(), socketAddress);
        //		} finally {
        //			ReleaseUtil.release(buf);
        //		}
    }

    @Override
    public void sendPacket(DatagramPacket packet) throws IOException {
        sendPacket(packet, remote);
    }

    //	private ByteBuf allocate(DatagramPacket packet) {
    //
    //		if (packet.getTimestamp() == -1) {
    //
    //			int length = packet.getData().length;
    //
    //			ByteBuf buf = session.getByteBufAllocator().allocate(DatagramPacket.PACKET_HEADER + length);
    //			buf.skipBytes(DatagramPacket.PACKET_HEADER);
    //			buf.put(packet.getData());
    //			return buf;
    //		}
    //
    //		return allocate(packet.getTimestamp(), packet.getSequenceNo(), packet.getData());
    //	}
    //
    //	private ByteBuf allocate(long timestamp, int sequenceNO, byte[] data) {
    //
    //		ByteBuf buf = session.getByteBufAllocator().allocate(DatagramPacket.PACKET_MAX);
    //
    //		buf.putLong(0);
    //		buf.putInt(sequenceNO);
    //		buf.put(data);
    //
    //		return buf;
    //	}

    @Override
    public boolean inSelectorLoop() {
        return selectorLoop.inEventLoop();
    }

}
