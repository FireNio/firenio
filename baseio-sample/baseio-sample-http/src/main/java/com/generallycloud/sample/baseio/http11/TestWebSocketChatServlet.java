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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.codec.http11.future.WebSocketFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.container.http11.service.HttpFutureAcceptorService;
import com.generallycloud.baseio.protocol.Future;

//FIXME ________根据当前是否正在redeploy来保存和恢复client
public class TestWebSocketChatServlet extends HttpFutureAcceptorService {

    private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter();

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {

        future.updateWebSocketProtocol();

        session.flush(future);
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        if (future instanceof HttpFuture) {
            super.accept(session, future);
            return;
        }

        WebSocketFuture f = (WebSocketFuture) future;

        // CLOSE
        if (f.isCloseFrame()) {

            if (!msgAdapter.removeClient(session)) {
                return;
            }

            JSONObject obj = new JSONObject();

            obj.put("username", session.getAttribute("username"));
            obj.put("numUsers", msgAdapter.getClientSize());
            obj.put("action", "user-left");

            String msg1 = obj.toJSONString();

            msgAdapter.sendMsg(msg1);

        } else {

            String msg = f.getReadText();

            JSONObject obj = JSON.parseObject(msg);

            String action = obj.getString("action");

            if ("new-message".equals(action)) {

                String owner = (String) session.getAttribute("username");

                String message = obj.getString("message");

                if (message.charAt(0) == '@') {

                    int nIndex = message.indexOf(' ');

                    if (nIndex > 1) {

                        String username = message.substring(1, nIndex);

                        SocketSession s = msgAdapter.getSession(username);

                        if (s == null) {
                            obj.put("message", "用户不存在或者已离线");
                            obj.put("username", owner);
                            msgAdapter.sendMsg(session, obj.toJSONString());
                            return;
                        }

                        obj.put("username", owner);

                        msgAdapter.sendMsg(session, obj.toJSONString());

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

                String username = (String) session.getAttribute("username");

                if (username != null) {
                    return;
                }

                username = obj.getString("username");

                if (StringUtil.isNullOrBlank(username)) {
                    return;
                }

                msgAdapter.addClient(username, session);

                session.setAttribute("username", username);

                obj.put("numUsers", msgAdapter.getClientSize());
                obj.put("action", "login");

                msgAdapter.sendMsg(session, obj.toJSONString());

                obj.put("username", username);
                obj.put("action", "user-joined");

                msgAdapter.sendMsg(obj.toJSONString());

                obj.put("action", "new-message");

                obj.put("username", "系统消息");

                obj.put("message", "欢迎加入QQ群讨论java io相关技术：540637859，@某人可以单独向他发送消息。");

                msgAdapter.sendMsg(session, obj.toJSONString());

            } else if ("typing".equals(action)) {

                obj.put("username", session.getAttribute("username"));

                String msg1 = obj.toJSONString();

                msgAdapter.sendMsg(msg1);

            } else if ("stop-typing".equals(action)) {

                obj.put("username", session.getAttribute("username"));

                String msg1 = obj.toJSONString();

                msgAdapter.sendMsg(msg1);

            } else if ("disconnect".equals(action)) {

                msgAdapter.removeClient(session);

                obj.put("username", session.getAttribute("username"));
                obj.put("numUsers", msgAdapter.getClientSize());
                obj.put("action", "user-left");

                String msg1 = obj.toJSONString();

                msgAdapter.sendMsg(msg1);
            } else {

                f.write("no action matched:" + action);

                session.flush(f);
            }
        }
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        this.msgAdapter.startup("WebSocketChat");

        super.initialize(context, config);
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {

        LifeCycleUtil.stop(msgAdapter);

        super.destroy(context, config);
    }
    
    /**
     * @return the msgAdapter
     */
    public WebSocketMsgAdapter getMsgAdapter() {
        return msgAdapter;
    }
}
