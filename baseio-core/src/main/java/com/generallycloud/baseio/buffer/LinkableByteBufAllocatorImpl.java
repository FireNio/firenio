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
package com.generallycloud.baseio.buffer;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.concurrent.Linkable;

public class LinkableByteBufAllocatorImpl extends AbstractLifeCycle
        implements LinkAbleByteBufAllocator {

    private ByteBufAllocator         allocator;

    private int                      index;

    private boolean                  isValidate;

    private LinkAbleByteBufAllocator next;

    public LinkableByteBufAllocatorImpl(ByteBufAllocator allocator, int index) {
        this.index = index;
        this.allocator = allocator;
    }

    @Override
    public ByteBuf allocate(int capacity) {
        ByteBuf buf = unwrap().allocate(capacity);
        if (buf == null) {
            return getNext().allocate(capacity, this);
        }
        return buf;
    }

    @Override
    public ByteBuf allocate(int limit, int maxLimit) {
        return unwrap().allocate(limit, maxLimit);
    }

    @Override
    public ByteBuf allocate(int capacity, LinkAbleByteBufAllocator allocator) {
        if (allocator == this) {
            //FIXME 是否申请java内存
            return UnpooledByteBufAllocator.getHeapInstance().allocate(capacity);
            //			return null;
        }
        ByteBuf buf = unwrap().allocate(capacity);
        if (buf == null) {
            return getNext().allocate(capacity, allocator);
        }
        return buf;
    }

    @Override
    protected void doStart() throws Exception {
        unwrap().start();
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(unwrap());
    }

    @Override
    public void freeMemory() {
        unwrap().freeMemory();
    }

    @Override
    public int getCapacity() {
        return unwrap().getCapacity();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public LinkAbleByteBufAllocator getNext() {
        return next;
    }

    @Override
    public int getUnitMemorySize() {
        return unwrap().getUnitMemorySize();
    }

    @Override
    public boolean isDirect() {
        return unwrap().isDirect();
    }

    @Override
    public boolean isValidate() {
        return isValidate;
    }

    @Override
    protected boolean logger() {
        return false;
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit) {
        return unwrap().reallocate(buf, limit);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld) {
        return unwrap().reallocate(buf, limit, copyOld);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit) {
        return unwrap().reallocate(buf, limit, maxLimit);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
        return unwrap().reallocate(buf, limit, maxLimit, copyOld);
    }

    @Override
    public void release(ByteBuf buf) {
        unwrap().release(buf);
    }

    @Override
    public void setNext(Linkable next) {
        this.next = (LinkAbleByteBufAllocator) next;
    }

    @Override
    public void setValidate(boolean validate) {
        this.isValidate = validate;
    }

    @Override
    public String toString() {
        return unwrap().toString();
    }

    @Override
    public ByteBufAllocator unwrap() {
        return allocator;
    }

}
