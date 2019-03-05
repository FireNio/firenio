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

import java.util.Scanner;

import com.firenio.baseio.codec.lengthvalue.LengthValueCodec;
import com.firenio.baseio.codec.lengthvalue.LengthValueFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.Channel;

public class TestLengthValueClientPush {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                System.out.println(">msg from server: " + frame);
            }
        };
        ChannelConnector context = new ChannelConnector(8300);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new LengthValueCodec());
        Channel ch = context.connect();
        Util.exec(new Runnable() {

            @Override
            public void run() {
                System.out.println("************************************************");
                System.out.println("提示:");
                System.out.println("list(获取所有客户端id)");
                System.out.println("id(获取当前客户端id)");
                System.out.println("push id msg(推送消息到)");
                System.out.println("broadcast msg(广播消息)");
                System.out.println("exit(退出客户端)");
                System.out.println("仅用于演示，msg请勿包含空格");
                System.out.println("************************************************");
                Scanner scanner = new Scanner(System.in);
                for (;;) {
                    System.out.println(">");
                    String line = scanner.nextLine();
                    if ("exit".equals(line)) {
                        Util.close(ch);
                        break;
                    }
                    LengthValueFrame frame = new LengthValueFrame();
                    frame.write(line, context);
                    try {
                        ch.writeAndFlush(frame);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
