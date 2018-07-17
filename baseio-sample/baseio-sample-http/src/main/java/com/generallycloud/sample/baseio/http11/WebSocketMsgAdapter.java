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

import com.generallycloud.baseio.codec.http11.WebSocketFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class WebSocketMsgAdapter extends AbstractEventLoop {

    private Logger                        logger     = LoggerFactory.getLogger(getClass());
    private List<NioSocketChannel>        clients    = new ArrayList<>();
    private Map<String, NioSocketChannel> clientsMap = new HashMap<>();
    private BlockingQueue<Msg>            msgs       = new ArrayBlockingQueue<>(1024 * 4);

    public synchronized void addClient(String username, NioSocketChannel channel) {
        clients.add(channel);
        clientsMap.put(username, channel);
        logger.info("client joined {} ,clients size: {}", channel, clients.size());
    }

    public synchronized boolean removeClient(NioSocketChannel channel) {
        if (clients.remove(channel)) {
            String username = (String) channel.getAttribute("username");
            if (!StringUtil.isNullOrBlank(username)) {
                clientsMap.remove(username);
            }
            logger.info("client left {} ,clients size: {}", channel, clients.size());
            return true;
        }
        return false;
    }

    public NioSocketChannel getChannel(String username) {
        return clientsMap.get(username);
    }

    public void sendMsg(String msg) {
        sendMsg(null, msg);
    }

    public void sendMsg(NioSocketChannel channel, String msg) {
        msgs.offer(new Msg(channel, msg));
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
            NioSocketChannel channel = msg.channel;
            if (channel != null) {
                WebSocketFuture f = new WebSocketFuture();
                f.write(msg.msg, channel);
                channel.flush(f);
            } else {
                for (int i = 0; i < clients.size(); i++) {
                    NioSocketChannel s = clients.get(i);
                    if (s.isOpened()) {
                        WebSocketFuture f = new WebSocketFuture();
                        f.write(msg.msg, s);
                        s.flush(f);
                    } else {
                        removeClient(s);
                        i--;
                    }
                }
            }
        }
    }

    class Msg {

        Msg(NioSocketChannel channel, String msg) {
            this.msg = msg;
            this.channel = channel;
        }

        String           msg;
        NioSocketChannel channel;
    }
}
