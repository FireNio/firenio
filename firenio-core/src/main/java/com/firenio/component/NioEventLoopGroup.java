/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.component;

import java.io.IOException;

import com.firenio.buffer.ByteBufAllocator;
import com.firenio.buffer.ByteBufAllocatorGroup;
import com.firenio.buffer.UnpooledByteBufAllocator;
import com.firenio.common.Util;
import com.firenio.component.NioEventLoop.EpollEventLoop;
import com.firenio.component.NioEventLoop.JavaEventLoop;
import com.firenio.concurrent.EventLoopGroup;
import com.firenio.concurrent.FixedAtomicInteger;

/**
 * @author wangkai
 * 注意：如需共享group，且group担当acceptor和connector时，一定要先起acceptor，
 * 或者显示调用group.setAcceptor(true)
 */
public class NioEventLoopGroup extends EventLoopGroup {

    private final boolean               acceptor;
    private       ByteBufAllocatorGroup allocatorGroup;
    private       FixedAtomicInteger    channelIds;
    private       int                   channelReadBuffer      = 1024 * 512;
    //允许的最大连接数(单核)
    private       int                   channelSizeLimit       = 1024 * 64;
    private       boolean               concurrentFrameStack   = true;
    private       ChannelContext        context;
    private       boolean               enableMemoryPool       = true;
    //内存池是否使用启用堆外内存
    private       boolean               enableMemoryPoolDirect = true;
    private       NioEventLoop[]        eventLoops;
    private       long                  idleTime               = 30 * 1000;
    //内存池内存单元数量(单核)
    private       int                   memoryPoolCapacity;
    private       int                   memoryPoolRate         = 32;
    //内存池单元大小
    private       int                   memoryPoolUnit         = 512;
    private       boolean               sharable;
    //单条连接write(srcs)的数量
    private       int                   writeBuffers           = 32;

    public NioEventLoopGroup() {
        this(false);
    }

    public NioEventLoopGroup(boolean sharable) {
        this(sharable, Util.availableProcessors() / 2);
    }

    public NioEventLoopGroup(boolean sharable, int eventLoopSize) {
        this(sharable, eventLoopSize, 30 * 1000);
    }

    public NioEventLoopGroup(boolean sharable, int eventLoopSize, int idleTime) {
        this(sharable, eventLoopSize, idleTime, false);
    }

    public NioEventLoopGroup(boolean sharable, int eventLoopSize, int idleTime, boolean acceptor) {
        super("nio-processor", eventLoopSize);
        this.acceptor = acceptor;
        this.idleTime = idleTime;
        this.sharable = sharable;
    }

    public NioEventLoopGroup(int eventLoopSize) {
        this(false, eventLoopSize);
    }

    public NioEventLoopGroup(int eventLoopSize, int idleTime) {
        this(false, eventLoopSize, idleTime);
    }

    public NioEventLoopGroup(String name) {
        this(name, false);
    }

    public NioEventLoopGroup(String name, boolean acceptor) {
        super(name, 1);
        this.acceptor = acceptor;
    }

    @Override
    protected void doStart() throws Exception {
        this.channelIds = new FixedAtomicInteger(0x1000, Integer.MAX_VALUE);
        if (memoryPoolCapacity == 0) {
            long total = Runtime.getRuntime().maxMemory();
            memoryPoolCapacity = (int) (total / (memoryPoolUnit * getEventLoopSize() * memoryPoolRate));
        }
        if (isEnableMemoryPool() && getAllocatorGroup() == null) {
            this.allocatorGroup = new ByteBufAllocatorGroup(getEventLoopSize(), memoryPoolCapacity, memoryPoolUnit, enableMemoryPoolDirect);
        }
        Util.start(getAllocatorGroup());
        super.doStart();
    }

    @Override
    protected void doStop() {
        Util.stop(allocatorGroup);
        super.doStop();
    }

    public ByteBufAllocatorGroup getAllocatorGroup() {
        return allocatorGroup;
    }

    public FixedAtomicInteger getChannelIds() {
        return channelIds;
    }

    public int getChannelReadBuffer() {
        return channelReadBuffer;
    }

    public void setChannelReadBuffer(int channelReadBuffer) {
        checkNotRunning();
        this.channelReadBuffer = channelReadBuffer;
    }

    public int getChannelSizeLimit() {
        return channelSizeLimit;
    }

    public void setChannelSizeLimit(int channelSizeLimit) {
        checkNotRunning();
        this.channelSizeLimit = channelSizeLimit;
    }

    public ChannelContext getContext() {
        return context;
    }

    protected void setContext(ChannelContext context) {
        this.context = context;
    }

    @Override
    public NioEventLoop getEventLoop(int index) {
        return eventLoops[index];
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        checkNotRunning();
        this.idleTime = idleTime;
    }

    public int getMemoryPoolCapacity() {
        return memoryPoolCapacity;
    }

    public void setMemoryPoolCapacity(int memoryPoolCapacity) {
        checkNotRunning();
        this.memoryPoolCapacity = memoryPoolCapacity;
    }

    public int getMemoryPoolRate() {
        return memoryPoolRate;
    }

    public void setMemoryPoolRate(int memoryPoolRate) {
        checkNotRunning();
        this.memoryPoolRate = memoryPoolRate;
    }

    public int getMemoryPoolUnit() {
        return memoryPoolUnit;
    }

    public void setMemoryPoolUnit(int memoryPoolUnit) {
        checkNotRunning();
        this.memoryPoolUnit = memoryPoolUnit;
    }

    @Override
    public NioEventLoop getNext() {
        return eventLoops[getNextEventLoopIndex()];
    }

    public ByteBufAllocator getNextByteBufAllocator(int index) {
        ByteBufAllocatorGroup group = allocatorGroup;
        if (group == null) {
            return UnpooledByteBufAllocator.get(isEnableMemoryPoolDirect());
        } else {
            return group.getAllocator(index);
        }
    }

    public int getWriteBuffers() {
        return writeBuffers;
    }

    public void setWriteBuffers(int writeBuffers) {
        checkNotRunning();
        this.writeBuffers = writeBuffers;
    }

    @Override
    protected NioEventLoop[] initEventLoops() {
        eventLoops = new NioEventLoop[getEventLoopSize()];
        return eventLoops;
    }

    public boolean isConcurrentFrameStack() {
        return concurrentFrameStack;
    }

    public void setConcurrentFrameStack(boolean concurrentFrameStack) {
        checkNotRunning();
        this.concurrentFrameStack = concurrentFrameStack;
    }

    public boolean isEnableMemoryPool() {
        return enableMemoryPool;
    }

    public void setEnableMemoryPool(boolean enableMemoryPool) {
        checkNotRunning();
        this.enableMemoryPool = enableMemoryPool;
    }

    public boolean isEnableMemoryPoolDirect() {
        return enableMemoryPoolDirect;
    }

    public void setEnableMemoryPoolDirect(boolean enableMemoryPoolDirect) {
        checkNotRunning();
        this.enableMemoryPoolDirect = enableMemoryPoolDirect;
    }

    public boolean isSharable() {
        return sharable;
    }

    @Override
    protected NioEventLoop newEventLoop(int index, String t_name) throws IOException {
        if (Native.EPOLL_AVAILABLE) {
            return new EpollEventLoop(this, index, t_name);
        } else {
            return new JavaEventLoop(this, index, t_name);
        }
    }

    protected boolean isAcceptor() {
        return acceptor;
    }

}
