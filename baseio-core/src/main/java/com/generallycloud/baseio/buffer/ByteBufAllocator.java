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

import com.generallycloud.baseio.LifeCycle;

//FIXME 考虑加入free链表
public interface ByteBufAllocator extends LifeCycle {

    public abstract void release(ByteBuf buf);

    public abstract ByteBuf allocate(int limit);

    public abstract ByteBuf allocate(int limit, int maxLimit);

    public abstract ByteBuf reallocate(ByteBuf buf, int limit);

    public abstract ByteBuf reallocate(ByteBuf buf, int limit, boolean copyOld);

    public abstract ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit);

    public abstract ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld);

    public abstract int getUnitMemorySize();

    public abstract void freeMemory();

    public abstract int getCapacity();

    public abstract boolean isDirect();

}
