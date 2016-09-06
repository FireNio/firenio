package com.test.service.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.Looper;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.component.protocol.http11.future.WebSocketTextReadFutureImpl;

public class WebSocketMsgAdapter implements Looper {

	private Logger				logger	= LoggerFactory.getLogger(WebSocketMsgAdapter.class);

	private List<Session>		clients	= new ArrayList<Session>();

	private ListQueue<String>	msgs		= new ListQueueABQ<String>(1024 * 4);
	
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
	
	public int getClientSize(){
		return clients.size();
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

					WebSocketReadFuture f = new WebSocketTextReadFutureImpl(s);

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
