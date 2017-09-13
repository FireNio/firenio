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
import java.util.Map;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFutureImpl;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.component.SocketSessionManager;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class SimpleTestFIxedLengthServerPush {
    
    private static Logger logger = LoggerFactory.getLogger(SimpleTestFIxedLengthServerPush.class);
    
    public static void main(String[] args) throws Exception {
        
        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                SocketSessionManager sessionManager = session.getContext().getSessionManager();
                Map<Integer, SocketSession> sessions = sessionManager.getManagedSessions();
                String msg = future.getReadText();
                String []arr = msg.split(" ");  
                String cmd = arr[0];
                logger.info("msg received: {}",msg);
                if ("list".equals(cmd)) {
                    String keys = sessions.keySet().toString();
                    future.write(keys);
                }else if ("id".equals(cmd)) {
                    future.write(String.valueOf(session.getSessionId()));
                }else if ("push".equals(cmd)) {
                    Integer id = Integer.valueOf(arr[1]);
                    SocketSession target = sessions.get(id);
                    if (target == null) {
                        future.write("offline id: "+id);
                    }else {
                        future.write("from [");
                        future.write(String.valueOf(session.getSessionId()));
                        future.write("] push msg>");
                        future.write(arr[2]);
                        target.flush(future);
                        return;
                    }
                }else if ("broadcast".equals(cmd)) {
                    future.write("from [");
                    future.write(String.valueOf(session.getSessionId()));
                    future.write("] broadcast msg>");
                    future.write(arr[1]);
                    sessionManager.broadcast(future);
                    return;
                }else {
                    future.write("no cmd: "+cmd);
                }
                session.flush(future);
            }
        };
        SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));
        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.addSessionEventListener(new SocketSessionEventListener() {
            
            @Override
            public void sessionOpened(SocketSession session) throws Exception {
            }
            
            @Override
            public void sessionClosed(SocketSession session) {
                SocketSessionManager sessionManager = session.getContext().getSessionManager();
                FixedLengthFuture future = new FixedLengthFutureImpl(session.getContext());
                future.write("client left: "+session.getSessionId());
                try {
                    sessionManager.broadcast(future);
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        });
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.setProtocolFactory(new FixedLengthProtocolFactory());
        acceptor.bind();
        
    }
    
}
