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
package com.generallycloud.baseio.codec.protobase;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.collection.Parameters;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ParamedProtobaseCodec extends ProtobaseCodec {

    public ParamedProtobaseCodec() {
        this(1024 * 64);
    }

    public ParamedProtobaseCodec(int limit) {
        super(limit);
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf buffer) {
        return new ParamedProtobaseFrame().setLimit(getLimit());
    }
    
    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        ParamedProtobaseFrame f = (ParamedProtobaseFrame) frame;
        Parameters p = f.getParameters();
        if (p != null) {
            f.write(p.toString(),ch);
        }
        return super.encode(ch, frame);
    }

    @Override
    public String getProtocolId() {
        return "ParamedProtobase";
    }

}
