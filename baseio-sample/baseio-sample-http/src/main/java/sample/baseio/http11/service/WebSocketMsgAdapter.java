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
package sample.baseio.http11.service;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.firenio.baseio.codec.http11.WebSocketFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ChannelManager;
import com.firenio.baseio.concurrent.EventLoop;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public class WebSocketMsgAdapter extends EventLoop {

    private Map<String, Channel>    clientsMap = new ConcurrentHashMap<>();

    private Logger                  logger     = LoggerFactory.getLogger(getClass());
    private BlockingQueue<Runnable> msgs       = new ArrayBlockingQueue<>(1024 * 4);

    public WebSocketMsgAdapter(String threadName) {
        super(threadName);
    }

    public void addClient(String username, Channel ch) {
        ch.setAttribute("username", username);
        clientsMap.put(username, ch);
        logger.info("client joined {} ,clients size: {}", ch, clientsMap.size());
    }

    @Override
    protected void doLoop() throws InterruptedException {
        Msg msg = (Msg) msgs.poll(16, TimeUnit.MILLISECONDS);
        if (msg == null) {
            return;
        }
        Channel ch = msg.ch;
        if (ch != null) {
            WebSocketFrame f = new WebSocketFrame();
            f.setContent(ch.allocate());
            f.write(msg.msg, ch);
            try {
                ch.writeAndFlush(f);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            if (!clientsMap.isEmpty()) {
                WebSocketFrame f = new WebSocketFrame();
                f.setString(msg.msg, Util.UTF8);
                try {
                    ChannelManager.broadcast(f, clientsMap.values());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public Channel getChannel(String username) {
        return clientsMap.get(username);
    }

    public int getClientSize() {
        return clientsMap.size();
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return msgs;
    }

    public boolean removeClient(Channel ch) {
        String username = (String) ch.getAttribute("username");
        if ((!Util.isNullOrBlank(username)) && clientsMap.remove(username) != null) {
            logger.info("client left {} ,clients size: {}", ch, clientsMap.size());
            return true;
        }
        return false;
    }

    public void sendMsg(Channel ch, String msg) {
        msgs.offer(new Msg(ch, msg));
    }

    public void sendMsg(String msg) {
        sendMsg(null, msg);
    }

    class Msg implements Runnable {

        Channel ch;

        String  msg;

        Msg(Channel ch, String msg) {
            this.msg = msg;
            this.ch = ch;
        }

        @Override
        public void run() {}

    }

}
