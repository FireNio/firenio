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
import com.generallycloud.baseio.codec.http11.future.WebSocketFutureImpl;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.container.http11.service.HttpFutureAcceptorService;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

// FIXME ________根据当前是否正在redeploy来保存和恢复client
public class TestWebSocketRumpetrollServlet extends HttpFutureAcceptorService {

    private Logger              logger     = LoggerFactory.getLogger(getClass());

    private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter();

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {

        future.updateWebSocketProtocol();

        session.flush(future);

        msgAdapter.addClient(getAddress(session.getIoSession()), session.getIoSession());

        SocketSession ioSession = session.getIoSession();

        JSONObject o = new JSONObject();
        o.put("type", "welcome");
        o.put("id", ioSession.getSessionId());

        WebSocketFuture f = new WebSocketFutureImpl(ioSession.getContext());

        f.write(o.toJSONString());

        session.flush(f);
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        if (future instanceof HttpFuture) {
            super.accept(session, future);
            return;
        }

        WebSocketFuture f = (WebSocketFuture) future;

        // CLOSE
        if (f.getType() == 8) {

            msgAdapter.removeClient(session);

            JSONObject o = new JSONObject();
            o.put("type", "closed");
            o.put("id", session.getSessionId());

            msgAdapter.sendMsg(o.toJSONString());

            logger.info("客户端主动关闭连接：{}", session);
        } else {

            String msg = f.getReadText();

            JSONObject o = JSON.parseObject(msg);

            String name = o.getString("name");

            if (StringUtil.isNullOrBlank(name)) {
                name = getAddress(session);
            }

            o.put("name", name);
            o.put("id", session.getSessionId());

            String type = o.getString("type");

            if ("update".equals(type)) {
                o.put("life", "1");
                o.put("authorized", "false");
                o.put("x", Double.valueOf(o.getString("x")));
                o.put("y", Double.valueOf(o.getString("x")));
                o.put("momentum", Double.valueOf(o.getString("momentum")));
                o.put("angle", Double.valueOf(o.getString("angle")));
            } else if ("message".equals(type)) {

            }

            msgAdapter.sendMsg(o.toJSONString());
        }
    }

    private String getAddress(SocketSession session) {

        String address = (String) session.getAttribute("_remote_address");

        if (address == null) {
            address = session.getRemoteSocketAddress().toString();

            session.setAttribute("_remote_address", address);
        }

        return address;
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        msgAdapter.startup("WebSocketRumpetroll");

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
