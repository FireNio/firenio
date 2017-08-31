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
package com.generallycloud.sample.baseio.http11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.generallycloud.baseio.codec.http11.future.WebSocketFuture;
import com.generallycloud.baseio.codec.http11.future.WebSocketFutureImpl;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class WebSocketMsgAdapter extends AbstractEventLoop {

    private Logger                     logger     = LoggerFactory.getLogger(getClass());

    private List<SocketSession>        clients    = new ArrayList<>();

    private Map<String, SocketSession> clientsMap = new HashMap<>();

    private BlockingQueue<Msg>         msgs       = new ArrayBlockingQueue<>(1024 * 4);

    public synchronized void addClient(String username, SocketSession session) {

        clients.add(session);

        clientsMap.put(username, session);

        logger.info("client joined {} ,clients size: {}", session, clients.size());
    }

    public synchronized boolean removeClient(SocketSession session) {

        if (clients.remove(session)) {

            String username = (String) session.getAttribute("username");

            if (!StringUtil.isNullOrBlank(username)) {
                clientsMap.remove(username);
            }

            logger.info("client left {} ,clients size: {}", session, clients.size());
            return true;
        }

        return false;
    }

    public SocketSession getSession(String username) {
        return clientsMap.get(username);
    }

    public void sendMsg(String msg) {
        sendMsg(null, msg);
    }

    public void sendMsg(SocketSession session, String msg) {
        msgs.offer(new Msg(session, msg));
    }

    public int getClientSize() {
        return clients.size();
    }

    @Override
    protected void doLoop() throws InterruptedException {

        Msg msg = msgs.poll(16, TimeUnit.MILLISECONDS);

        if (msg == null) {
            return;
        }

        synchronized (this) {

            SocketSession session = msg.session;

            if (session != null) {

                WebSocketFuture f = new WebSocketFutureImpl(session.getContext());

                f.write(msg.msg);

                session.flush(f);

                return;
            }

            for (int i = 0; i < clients.size(); i++) {

                SocketSession s = clients.get(i);

                if (s.isOpened()) {

                    WebSocketFuture f = new WebSocketFutureImpl(s.getContext());

                    f.write(msg.msg);

                    s.flush(f);
                } else {

                    removeClient(s);

                    i--;
                }
            }
        }
    }

    class Msg {

        Msg(SocketSession session, String msg) {
            this.msg = msg;
            this.session = session;
        }

        String        msg;
        SocketSession session;
    }
}
