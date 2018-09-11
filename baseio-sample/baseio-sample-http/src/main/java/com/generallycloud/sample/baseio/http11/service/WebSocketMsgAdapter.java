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
package com.generallycloud.sample.baseio.http11.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.generallycloud.baseio.codec.http11.WebSocketFrame;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelManager;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class WebSocketMsgAdapter extends AbstractEventLoop {

    private Logger                        logger     = LoggerFactory.getLogger(getClass());
    private List<NioSocketChannel>        clients    = new ArrayList<>();
    private Map<String, NioSocketChannel> clientsMap = new HashMap<>();
    private BlockingQueue<Msg>            msgs       = new ArrayBlockingQueue<>(1024 * 4);

    public synchronized void addClient(String username, NioSocketChannel ch) {
        clients.add(ch);
        clientsMap.put(username, ch);
        logger.info("client joined {} ,clients size: {}", ch, clients.size());
    }

    public synchronized boolean removeClient(NioSocketChannel ch) {
        if (clients.remove(ch)) {
            String username = (String) ch.getAttribute("username");
            if (!StringUtil.isNullOrBlank(username)) {
                clientsMap.remove(username);
            }
            logger.info("client left {} ,clients size: {}", ch, clients.size());
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

    public void sendMsg(NioSocketChannel ch, String msg) {
        msgs.offer(new Msg(ch, msg));
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
            NioSocketChannel ch = msg.ch;
            if (ch != null) {
                WebSocketFrame f = new WebSocketFrame();
                f.write(msg.msg, ch);
                ch.flush(f);
            } else {
                WebSocketFrame f = new WebSocketFrame();
                f.write(msg.msg, Encoding.UTF8);
                try {
                    ChannelManager.broadcast(f, clients);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    class Msg {

        Msg(NioSocketChannel ch, String msg) {
            this.msg = msg;
            this.ch = ch;
        }

        String           msg;
        NioSocketChannel ch;
    }
}
