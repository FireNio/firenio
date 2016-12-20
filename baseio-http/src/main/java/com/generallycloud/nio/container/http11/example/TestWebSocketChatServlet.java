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
package com.generallycloud.nio.container.http11.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFutureImpl;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.http11.service.HttpFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestWebSocketChatServlet extends HttpFutureAcceptorService {

	private WebSocketMsgAdapter msgAdapter = new WebSocketMsgAdapter();

	@Override
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();

		session.flush(future);

	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		if (future instanceof HttpReadFuture) {
			super.accept(session, future);
			return;
		}

		WebSocketReadFuture f = (WebSocketReadFuture) future;

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

			// String msg = getMsg(session, );

			String msg = f.getReadText();

			JSONObject obj = JSON.parseObject(msg);

			String action = obj.getString("action");

			if ("new-message".equals(action)) {

				obj.put("username", session.getAttribute("username"));

				String msg1 = obj.toJSONString();

				msgAdapter.sendMsg(msg1);

			} else if ("add-user".equals(action)) {

				msgAdapter.addClient(session);

				String username = (String) session.getAttribute("username");

				if (username != null) {
					return;
				}

				username = obj.getString("username");

				session.setAttribute("username", username);

				obj.put("numUsers", msgAdapter.getClientSize());
				obj.put("action", "login");

				String msg1 = obj.toJSONString();

				WebSocketReadFutureImpl f2 = new WebSocketTextReadFutureImpl(session.getContext());
				f2.write(msg1);
				session.flush(f2);

				obj.put("username", username);
				obj.put("action", "user-joined");

				String msg2 = obj.toJSONString();

				msgAdapter.sendMsg(msg2);

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
}
