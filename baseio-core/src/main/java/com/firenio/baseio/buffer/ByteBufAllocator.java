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
package com.firenio.baseio.buffer;

import com.firenio.baseio.LifeCycle;

//FIXME 考虑加入free链表
public abstract class ByteBufAllocator extends LifeCycle {

    public abstract ByteBuf allocate();

    public abstract ByteBuf allocate(int limit);

    protected abstract void expansion(ByteBuf buf, int cap);

    public abstract void freeMemory();

    public abstract int getCapacity();

    public abstract int getUnit();

    public abstract boolean isDirect();

    protected abstract void release(ByteBuf buf);

}
