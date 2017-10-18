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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.StringUtil;

public abstract class AbstractChannel implements Channel {

    static final InetSocketAddress ERROR_SOCKET_ADDRESS = new InetSocketAddress(0);

    protected String               edp_description;
    protected int                  channelId;
    protected InetSocketAddress    local;
    protected InetSocketAddress    remote;
    protected long                 lastAccess;
    protected long                 creationTime         = System.currentTimeMillis();
    protected ReentrantLock        closeLock            = new ReentrantLock();
    protected ByteBufAllocator     byteBufAllocator;

    public AbstractChannel(ByteBufAllocator allocator, ChannelContext context, int channelId) {
        // 认为在第一次Idle之前，连接都是畅通的
        this.byteBufAllocator = allocator;
        this.lastAccess = creationTime + context.getSessionIdleTime();
        this.channelId = channelId;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    @Override
    public String getLocalAddr() {

        InetAddress address = getLocalSocketAddress().getAddress();

        if (address == null) {
            return "127.0.0.1";
        }

        return address.getHostAddress();
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return byteBufAllocator;
    }

    protected abstract void physicalClose();

    @Override
    public String getLocalHost() {
        return getLocalSocketAddress().getHostName();
    }

    @Override
    public int getLocalPort() {
        return getLocalSocketAddress().getPort();
    }

    @Override
    public abstract InetSocketAddress getLocalSocketAddress();

    protected abstract String getMarkPrefix();

    @Override
    public String getRemoteAddr() {

        InetSocketAddress address = getRemoteSocketAddress();

        if (address == null) {

            return "closed";
        }

        return address.getAddress().getHostAddress();
    }

    /**
     * 请勿使用,可能出现阻塞
     * 
     * @see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6487744
     */
    @Override
    @Deprecated
    public String getRemoteHost() {

        InetSocketAddress address = getRemoteSocketAddress();

        if (address == null) {

            return "closed";
        }

        return address.getAddress().getHostName();
    }

    @Override
    public int getRemotePort() {

        InetSocketAddress address = getRemoteSocketAddress();

        if (address == null) {

            return -1;
        }

        return address.getPort();
    }

    @Override
    public String toString() {

        if (edp_description == null) {
            edp_description = new StringBuilder("[").append("Id(").append(getIdHexString(channelId))
                    .append(")R/").append(getRemoteAddr()).append(":").append(getRemotePort())
                    .append("; L:").append(getLocalPort()).append("]").toString();
        }

        return edp_description;
    }

    private String getIdHexString(int channelId) {

        String id = Long.toHexString(channelId);

        return "0x" + StringUtil.getZeroString(8 - id.length()) + id;
    }

    @Override
    public void active() {
        this.lastAccess = System.currentTimeMillis();
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getLastAccessTime() {
        return lastAccess;
    }

    protected ReentrantLock getCloseLock() {
        return closeLock;
    }

}
