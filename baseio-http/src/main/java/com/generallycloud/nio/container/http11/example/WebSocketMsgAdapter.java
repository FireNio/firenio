package com.generallycloud.nio.container.http11.example;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.AbstractEventLoopThread;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;

public class WebSocketMsgAdapter extends AbstractEventLoopThread {

	private Logger				logger	= LoggerFactory.getLogger(WebSocketMsgAdapter.class);

	private List<SocketSession>	clients	= new ArrayList<SocketSession>();

	private ListQueue<String>	msgs		= new ListQueueABQ<String>(1024 * 4);

	public synchronized void addClient(SocketSession session) {

		clients.add(session);

		logger.info("客户端 {} 已加入当前客户端数量：{}", session, clients.size());
	}

	public synchronized void removeClient(SocketSession session) {

		clients.remove(session);

		logger.info("客户端 {} 已离开当前客户端数量：{}", session, clients.size());
	}

	public void sendMsg(String msg) {
		msgs.offer(msg);
	}

	public int getClientSize() {
		return clients.size();
	}

	@Override
	protected void doLoop() {

		String msg = msgs.poll(16);

		if (msg == null) {
			return;
		}

		synchronized (this) {

			for (int i = 0; i < clients.size(); i++) {

				SocketSession s = clients.get(i);

				if (s.isOpened()) {

					WebSocketReadFuture f = new WebSocketTextReadFutureImpl(s.getContext());

					f.write(msg);

					s.flush(f);
				} else {

					removeClient(s);

					i--;
				}
			}
		}
	}
}
