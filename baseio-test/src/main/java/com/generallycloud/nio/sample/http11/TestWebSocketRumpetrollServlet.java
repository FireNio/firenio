/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.sample.http11;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.http11.service.HttpFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestWebSocketRumpetrollServlet extends HttpFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketRumpetrollServlet.class);

	private WebSocketMsgAdapter	msgAdapter	= new WebSocketMsgAdapter();

	@Override
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();
		
		session.flush(future);

		msgAdapter.addClient(session.getIoSession());

		SocketSession ioSession = session.getIoSession();

		JSONObject o = new JSONObject();
		o.put("type", "welcome");
		o.put("id", ioSession.getSessionID());

		WebSocketReadFuture f = new WebSocketTextReadFutureImpl(ioSession.getContext());

		f.write(o.toJSONString());

		session.flush(f);
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		if (future instanceof HttpReadFuture) {
			super.accept(session, future);
			return;
		}

		WebSocketReadFuture f = (WebSocketReadFuture) future;

		// CLOSE
		if (f.getType() == 8) {

			msgAdapter.removeClient(session);

			JSONObject o = new JSONObject();
			o.put("type", "closed");
			o.put("id", session.getSessionID());

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
			o.put("id", session.getSessionID());

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
}
