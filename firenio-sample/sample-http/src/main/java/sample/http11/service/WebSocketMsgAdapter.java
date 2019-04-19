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
package sample.http11.service;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.firenio.codec.http11.WebSocketFrame;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelManager;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public class WebSocketMsgAdapter extends EventLoop {

    private Map<Integer, Client>    clientMap  = new ConcurrentHashMap<>();
    private Map<String, Channel>    channelMap = new ConcurrentHashMap<>();
    private Logger                  logger     = LoggerFactory.getLogger(getClass());
    private BlockingQueue<Runnable> msgs       = new ArrayBlockingQueue<>(1024 * 4);

    public WebSocketMsgAdapter(String threadName) {
        super(threadName);
    }

    public void addClient(String username, Channel ch) {
        clientMap.put(ch.getChannelId(), new Client(ch, username));
        channelMap.put(username, ch);
        logger.info("client joined {} ,clients size: {}", ch, clientMap.size());
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
            if (!clientMap.isEmpty()) {
                WebSocketFrame f = new WebSocketFrame();
                f.setString(msg.msg, Util.UTF8);
                try {
                    ChannelManager.broadcast(f, channelMap.values());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public Client getClient(Integer id) {
        return clientMap.get(id);
    }

    public int getClientSize() {
        return clientMap.size();
    }

    @Override
    public BlockingQueue<Runnable> getJobs() {
        return msgs;
    }

    public Client removeClient(Channel ch) {
        Client client = clientMap.remove(ch.getChannelId());
        if (client != null) {
            channelMap.remove(client.getUsername());
            logger.info("client left {} ,clients size: {}", ch, clientMap.size());
        }
        return client;
    }

    public void sendMsg(Channel ch, String msg) {
        msgs.offer(new Msg(ch, msg));
    }

    public void sendMsg(String msg) {
        sendMsg(null, msg);
    }

    public Channel getChannel(String username) {
        return channelMap.get(username);
    }

    static class Client {

        private Channel channel;
        private String  username;
        public Client(Channel ch, String username) {
            this.channel = ch;
            this.username = username;
        }

        public Channel getChannel() {
            return channel;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

    }

    class Msg implements Runnable {

        Channel ch;

        String msg;

        Msg(Channel ch, String msg) {
            this.msg = msg;
            this.ch = ch;
        }

        @Override
        public void run() {}

    }

}
