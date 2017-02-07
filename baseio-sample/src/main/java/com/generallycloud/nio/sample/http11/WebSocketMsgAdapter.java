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

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;

public class WebSocketMsgAdapter extends AbstractEventLoop {

	private Logger				logger	= LoggerFactory.getLogger(WebSocketMsgAdapter.class);

	private List<SocketSession>	clients	= new ArrayList<SocketSession>();

	private ListQueue<Msg>		msgs		= new ListQueueABQ<Msg>(1024 * 4);

	public synchronized void addClient(SocketSession session) {

		clients.add(session);

		logger.info("客户端 {} 已加入当前客户端数量：{}", session, clients.size());
	}

	public synchronized boolean removeClient(SocketSession session) {

		if (clients.remove(session)) {
			logger.info("客户端 {} 已离开当前客户端数量：{}", session, clients.size());
			return true;
		}

		return false;
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
	protected void doLoop() {

		Msg msg = msgs.poll(16);

		if (msg == null) {
			return;
		}

		synchronized (this) {

			SocketSession session = msg.session;

			if (session != null) {

				WebSocketReadFuture f = new WebSocketTextReadFutureImpl(session.getContext());

				f.write(msg.msg);

				session.flush(f);

				return;
			}

			for (int i = 0; i < clients.size(); i++) {

				SocketSession s = clients.get(i);

				if (s.isOpened()) {

					WebSocketReadFuture f = new WebSocketTextReadFutureImpl(s.getContext());

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

		String		msg;
		SocketSession	session;
	}
}
