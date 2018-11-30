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
package com.generallycloud.test.io.fixedlength;

import java.io.IOException;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFrame;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public class SimpleTestFIxedLengthBroadcastServer {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                FixedLengthFrame f = (FixedLengthFrame) frame;
                frame.write("yes server already accept your message:", channel);
                frame.write(f.getReadText(), channel);
                channel.flush(f);
            }
        };

        ChannelAcceptor context = new ChannelAcceptor(8300);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new SetOptionListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new FixedLengthCodec());
        context.bind();

        Util.exec(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    Util.sleep(1000);
                    FixedLengthFrame frame = new FixedLengthFrame();
                    frame.write("broadcast msg .........................", context);
                    try {
                        context.broadcast(frame);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
