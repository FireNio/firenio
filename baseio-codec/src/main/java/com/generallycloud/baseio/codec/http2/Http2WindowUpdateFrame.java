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
package com.generallycloud.baseio.codec.http2;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.MathUtil;

public class Http2WindowUpdateFrame extends Http2FrameHeader {

    private int updateValue;

    @Override
    Http2WindowUpdateFrame decode(Http2Session session, ByteBuf src, int length) {
        this.updateValue = MathUtil.int2int31(src.getInt());
        return this;
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public Http2FrameType getHttp2FrameType() {
        return Http2FrameType.FRAME_TYPE_SETTINGS;
    }

    public int getUpdateValue() {
        return updateValue;
    }

}
