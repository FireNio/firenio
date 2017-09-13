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

import java.util.Scanner;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.Future;

public class SimpleTestFIxedLengthClientPush {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                System.out.println(">msg from server: " + future.getReadText());
            }
        };
        SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration("localhost", 18300));
        SocketChannelConnector connector = new SocketChannelConnector(context);
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolFactory(new FixedLengthProtocolFactory());
        SocketSession session = connector.connect();
        ThreadUtil.execute(new Runnable() {
            
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
                for(;;) {
                    System.out.println(">");
                    String line = scanner.nextLine();
                    if ("exit".equals(line)) {
                        CloseUtil.close(session);
                        break;
                    }
                    FixedLengthFuture future = new FixedLengthFutureImpl(context);
                    future.write(line);
                    session.flush(future);
                }
            }
        });
    }
    
}
