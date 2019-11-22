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
package test.io.lenthvalue;

import com.firenio.buffer.ByteBuf;
import com.firenio.codec.lengthvalue.LengthValueCodec;
import com.firenio.codec.lengthvalue.LengthValueFrame;
import com.firenio.component.Channel;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;

/**
 * @author: wangkai
 **/
public class TestByteBufPool {

    public static void main(String[] args) throws Exception {

        ChannelConnector context = new ChannelConnector("192.168.1.102", 6500);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new LengthValueCodec());
        Channel ch  = context.connect(3000);
        ByteBuf buf = ch.allocate();
        ch.close();
        buf.release();

    }


}
