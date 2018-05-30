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

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.buffer.ByteBufAllocatorGroup;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorGroup;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocatorGroup;
import com.generallycloud.baseio.concurrent.AbstractEventLoopGroup;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;

/**
 * @author wangkai
 *
 */
public class SelectorEventLoopGroup extends AbstractEventLoopGroup {

    private int                   bufRecycleSize         = 1024 * 4;
    private ByteBufAllocatorGroup allocatorGroup;
    private int                   channelReadBuffer      = 1024 * 512;
    private boolean               enableMemoryPool       = true;
    //内存池是否使用启用堆外内存
    private boolean               enableMemoryPoolDirect = true;
    private long                  idleTime               = 30 * 1000;
    //内存池内存单元数量（单核）
    private int                   memoryPoolCapacity;
    private int                   memoryPoolRate         = 32;
    //内存池单元大小
    private int                   memoryPoolUnit         = 512;
    private SelectorEventLoop[]   eventLoops;
    //单条连接write(srcs)的数量
    private int                   writeBuffers           = 8;
    private FixedAtomicInteger    channelIds;
    private boolean               enableSsl;
    private boolean               sharable;
    private ChannelContext        context;
    private SelectorEventLoop     acceptorEventLoop;

    public SelectorEventLoopGroup() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public SelectorEventLoopGroup(int eventLoopSize) {
        super("nio-process", eventLoopSize);
    }

    private FixedAtomicInteger createChannelIdsSequence(int eventLoopSize) {
        int min = (10000 / eventLoopSize) * eventLoopSize - 1;
        int max = (Integer.MAX_VALUE / eventLoopSize) * eventLoopSize - 1;
        return new FixedAtomicInteger(min, max);
    }

    @Override
    protected void doStart() throws Exception {
        this.channelIds = createChannelIdsSequence(getEventLoopSize());
        if (memoryPoolCapacity == 0) {
            long total = Runtime.getRuntime().maxMemory();
            memoryPoolCapacity = (int) (total
                    / (memoryPoolUnit * getEventLoopSize() * memoryPoolRate));
        }
        this.initializeByteBufAllocator();
        if (sharable) {
            acceptorEventLoop = new SelectorEventLoop(this, -1, true);
            acceptorEventLoop.startup("nio-acceptor");
        }
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(acceptorEventLoop);
        super.doStop();
    }

    private void initializeByteBufAllocator() {
        if (getAllocatorGroup() == null) {
            if (isEnableMemoryPool()) {
                this.allocatorGroup = new PooledByteBufAllocatorGroup(this);
            } else {
                this.allocatorGroup = new UnpooledByteBufAllocatorGroup(this);
            }
        }
        LifeCycleUtil.start(getAllocatorGroup());
    }

    public int getBufRecycleSize() {
        return bufRecycleSize;
    }

    public ByteBufAllocatorGroup getAllocatorGroup() {
        return allocatorGroup;
    }

    public int getChannelReadBuffer() {
        return channelReadBuffer;
    }

    @Override
    public SelectorEventLoop getEventLoop(int index) {
        return eventLoops[index];
    }

    public long getIdleTime() {
        return idleTime;
    }

    public int getMemoryPoolCapacity() {
        return memoryPoolCapacity;
    }

    public int getMemoryPoolRate() {
        return memoryPoolRate;
    }

    public int getMemoryPoolUnit() {
        return memoryPoolUnit;
    }

    @Override
    public SelectorEventLoop getNext() {
        return eventLoops[getNextEventLoopIndex()];
    }

    public int getWriteBuffers() {
        return writeBuffers;
    }

    @Override
    protected SelectorEventLoop[] initEventLoops() {
        eventLoops = new SelectorEventLoop[getEventLoopSize()];
        return eventLoops;
    }

    public boolean isEnableMemoryPool() {
        return enableMemoryPool;
    }

    public boolean isEnableMemoryPoolDirect() {
        return enableMemoryPoolDirect;
    }

    @Override
    protected SelectorEventLoop newEventLoop(int index) {
        return new SelectorEventLoop(this, index, false);
    }

    public void setBufRecycleSize(int bufRecycleSize) {
        this.bufRecycleSize = bufRecycleSize;
    }

    public void setChannelReadBuffer(int channelReadBuffer) {
        this.channelReadBuffer = channelReadBuffer;
    }

    public void setEnableMemoryPool(boolean enableMemoryPool) {
        this.enableMemoryPool = enableMemoryPool;
    }

    public void setEnableMemoryPoolDirect(boolean enableMemoryPoolDirect) {
        this.enableMemoryPoolDirect = enableMemoryPoolDirect;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public void setMemoryPoolCapacity(int memoryPoolCapacity) {
        this.memoryPoolCapacity = memoryPoolCapacity;
    }

    public void setMemoryPoolRate(int memoryPoolRate) {
        this.memoryPoolRate = memoryPoolRate;
    }

    public void setMemoryPoolUnit(int memoryPoolUnit) {
        this.memoryPoolUnit = memoryPoolUnit;
    }

    public void setWriteBuffers(int writeBuffers) {
        this.writeBuffers = writeBuffers;
    }

    public FixedAtomicInteger getChannelIds() {
        return channelIds;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public void registSelector(ChannelContext context) throws IOException {
        if (sharable) {
            acceptorEventLoop.registSelector(context);
        } else {
            for (SelectorEventLoop eventLoop : eventLoops) {
                eventLoop.registSelector(context);
            }
        }
    }

    public boolean isSharable() {
        return sharable;
    }

    public void setSharable(boolean sharable) {
        this.sharable = sharable;
    }

    public ChannelContext getContext() {
        return context;
    }

    public void setContext(ChannelContext context) {
        this.context = context;
    }

}
