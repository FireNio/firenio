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
package test.io.lenthvalue;

import com.firenio.baseio.codec.lengthvalue.LengthValueCodec;
import com.firenio.baseio.codec.lengthvalue.LengthValueFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.Channel;

public class TestLengthValueClient1 {

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                LengthValueFrame f = (LengthValueFrame) frame;
                System.out.println();
                System.out.println("____________________" + f.getStringContent());
                System.out.println();
            }

        };
        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new LengthValueCodec());

        Channel ch = context.connect();
        StringBuilder sb = new StringBuilder(1024 * 6);
        for (int i = 0; i < 1; i++) {
            sb.append("hello!");
        }

        for (int i = 0; i < 20; i++) {
            LengthValueFrame frame = new LengthValueFrame();
            frame.write(sb.toString(), ch);
            ch.writeAndFlush(frame);
        }
        Util.sleep(100);
        Util.close(context);
    }

}
