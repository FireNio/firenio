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
package com.generallycloud.baseio.protocol;

import com.generallycloud.baseio.buffer.ByteBuf;

/**
 * @author wangkai
 *
 */
//FIXME add unsupported operation
public class DuplicateFuture extends DefaultFuture {

    private Future prototype;

    public DuplicateFuture(ByteBuf buf, Future prototype) {
        super(buf);
        this.prototype = prototype;
        //        this.flushed = prototype.flushed();
        //        this.isHeartbeat = prototype.isHeartbeat();
        this.setNeedSsl(prototype.isNeedSsl());
        //        this.isPING = prototype.isPING();
        //        this.isSilent = prototype.isSilent();
        //        this.readText = prototype.getReadText();
        //        this.writeBuffer = prototype.getWriteBuffer();
        //        this.writeSize = prototype.getWriteSize();
        //FIXME 放开这段代码
    }

    private Future unwrap() {
        return prototype;
    }

    @Override
    public Future duplicate() {
        return unwrap().duplicate();
    }

    @Override
    public int getWriteSize() {
        return unwrap().getWriteSize();
    }

    @Override
    public byte[] getWriteBuffer() {
        return unwrap().getWriteBuffer();
    }

}
