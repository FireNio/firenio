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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.firenio.baseio.codec.http11.HttpFrame;
import com.firenio.baseio.codec.http11.WebSocketFrame;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.Channel;

import sample.baseio.http11.HttpFrameAcceptor;

//FIXME ________根据当前是否正在redeploy来保存和恢复client
@Service("/web-socket-chat")
public class TestWebSocketChatServlet extends HttpFrameAcceptor {

    private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter("websocket-chat");

    @Override
    public void accept(Channel ch, Frame frame) throws Exception {
        if (frame instanceof HttpFrame) {
            ((HttpFrame) frame).updateWebSocketProtocol(ch);
            return;
        }
        WebSocketFrame f = (WebSocketFrame) frame;
        // CLOSE
        if (f.isCloseFrame()) {
            if (msgAdapter.removeClient(ch)) {
                JSONObject obj = new JSONObject();
                obj.put("username", ch.getAttribute("username"));
                obj.put("numUsers", msgAdapter.getClientSize());
                obj.put("action", "user-left");
                String msg1 = obj.toJSONString();
                msgAdapter.sendMsg(msg1);
            }
            if (ch.isOpened()) {
                ch.writeAndFlush(f);
            }
        } else {
            String msg = f.getStringContent();
            JSONObject obj = JSON.parseObject(msg);
            String action = obj.getString("action");
            if ("new-message".equals(action)) {
                String owner = (String) ch.getAttribute("username");
                String message = obj.getString("message");
                if (message.charAt(0) == '@') {
                    int nIndex = message.indexOf(' ');
                    if (nIndex > 1) {
                        String username = message.substring(1, nIndex);
                        Channel s = msgAdapter.getChannel(username);
                        if (s == null) {
                            obj.put("message", "用户不存在或者已离线");
                            obj.put("username", owner);
                            msgAdapter.sendMsg(ch, obj.toJSONString());
                            return;
                        }
                        obj.put("username", owner);
                        msgAdapter.sendMsg(ch, obj.toJSONString());
                        obj.put("username", owner + "@你");
                        obj.put("message", message.substring(nIndex));
                        msgAdapter.sendMsg(s, obj.toJSONString());
                        return;
                    }
                }
                obj.put("username", owner);
                String msg1 = obj.toJSONString();
                msgAdapter.sendMsg(msg1);
            } else if ("add-user".equals(action)) {
                String username = (String) ch.getAttribute("username");
                if (username != null) {
                    return;
                }
                username = obj.getString("username");
                if (Util.isNullOrBlank(username)) {
                    return;
                }
                msgAdapter.addClient(username, ch);
                ch.setAttribute("username", username);
                obj.put("numUsers", msgAdapter.getClientSize());
                obj.put("action", "login");
                msgAdapter.sendMsg(ch, obj.toJSONString());
                obj.put("username", username);
                obj.put("action", "user-joined");
                msgAdapter.sendMsg(obj.toJSONString());
                obj.put("action", "new-message");
                obj.put("username", "系统消息");
                obj.put("message", "欢迎加入QQ群讨论java io相关技术：540637859，@某人可以单独向他发送消息。");
                msgAdapter.sendMsg(ch, obj.toJSONString());
            } else if ("typing".equals(action)) {
                obj.put("username", ch.getAttribute("username"));
                String msg1 = obj.toJSONString();
                msgAdapter.sendMsg(msg1);
            } else if ("stop-typing".equals(action)) {
                obj.put("username", ch.getAttribute("username"));
                String msg1 = obj.toJSONString();
                msgAdapter.sendMsg(msg1);
            } else if ("disconnect".equals(action)) {
                msgAdapter.removeClient(ch);
                obj.put("username", ch.getAttribute("username"));
                obj.put("numUsers", msgAdapter.getClientSize());
                obj.put("action", "user-left");
                String msg1 = obj.toJSONString();
                msgAdapter.sendMsg(msg1);
            } else {
                f.write("no action matched:" + action, ch);
                ch.writeAndFlush(f);
            }
        }
    }

    @PostConstruct
    public void init() throws Exception {
        Util.start(msgAdapter);
    }

    @PreDestroy
    public void destroy() throws Exception {
        Util.stop(msgAdapter);
    }

    public WebSocketMsgAdapter getMsgAdapter() {
        return msgAdapter;
    }

}
