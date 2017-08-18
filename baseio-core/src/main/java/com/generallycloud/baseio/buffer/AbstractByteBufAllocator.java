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

public abstract class AbstractByteBufAllocator extends AbstractLifeCycle
        implements ByteBufAllocator {

    protected boolean isDirect;

    public AbstractByteBufAllocator(boolean isDirect) {
        this.isDirect = isDirect;
    }

    @Override
    public ByteBuf allocate(int limit, int maxLimit) {
        if (limit > maxLimit) {
            throw new BufferException("limit:" + limit + ",maxLimit:" + maxLimit);
        }
        return allocate(limit);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit, boolean copyOld) {
        if (limit > maxLimit) {
            throw new BufferException("limit:" + limit + ",maxLimit:" + maxLimit);
        }
        return reallocate(buf, limit, copyOld);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit) {
        return reallocate(buf, limit, false);
    }

    @Override
    public ByteBuf reallocate(ByteBuf buf, int limit, int maxLimit) {
        return reallocate(buf, limit, maxLimit, false);
    }

    @Override
    public boolean isDirect() {
        return isDirect;
    }
}
