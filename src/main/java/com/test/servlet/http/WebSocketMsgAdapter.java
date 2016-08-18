package com.test.servlet.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.Looper;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListABQ;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFutureImpl;

public class WebSocketMsgAdapter implements Looper {

	private Logger				logger	= LoggerFactory.getLogger(WebSocketMsgAdapter.class);

	private List<Session>		clients	= new ArrayList<Session>();

	private LinkedList<String>	msgs		= new LinkedListABQ<String>(1024 * 4);

	public void stop() {

	}

	public synchronized void addClient(Session session) {

		clients.add(session);

		logger.info("当前客户端数量：{}", clients.size());
	}

	public synchronized void removeClient(Session session) {

		clients.remove(session);

		logger.info("当前客户端数量：{}", clients.size());
	}

	public void sendMsg(String msg) {
		msgs.offer(msg);
	}

	public void loop() {

		String msg = msgs.poll(16);

		if (msg == null) {
			return;
		}

		synchronized (this) {

			for (int i = 0; i < clients.size(); i++) {

				Session s = clients.get(i);

				if (s.isOpened()) {

					WebSocketReadFuture f = new WebSocketReadFutureImpl(s);

					f.write(msg.getBytes(Encoding.UTF8));

					try {
						s.flush(f);
					} catch (IOException e) {
						logger.error(e.getMessage(),e);
					}
				} else {

					removeClient(s);

					i--;
				}
			}
		}
	}
}
