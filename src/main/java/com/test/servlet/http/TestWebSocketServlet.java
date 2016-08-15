package com.test.servlet.http;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.Looper;
import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListABQ;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFutureImpl;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestWebSocketServlet extends HTTPFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketServlet.class);

	private MsgAdapter		msgAdapter	= new MsgAdapter();

	private UniqueThread	msgAdapterThread;

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		String token = future.getRequestParam("token");

		if ("WebSocket".equals(token)) {
			future.updateWebSocketProtocol();
			msgAdapter.addClient(session.getIOSession());
		} else {
			future.write("illegal argement token:" + token);
		}

		session.flush(future);
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		if (future instanceof HttpReadFuture) {
			super.accept(session, future);
			return;
		}

		WebSocketReadFuture f = (WebSocketReadFuture) future;

		// CLOSE
		if (f.getType() == 8) {

			msgAdapter.removeClient(session);
			
			logger.info("客户端主动关闭连接：{}",session);
		} else {

			StringBuilder b = new StringBuilder();

			String address = getAddress(session);

			b.append("[");

			b.append(address);

			b.append("][");
			
			b.append(DateUtil.now());

			b.append("]:");
			
			b.append(f.getData().toString(Encoding.UTF8));

			msgAdapter.sendMsg(b.toString());
		}
	}

	private String getAddress(Session session) {

		String address = (String) session.getAttribute("_remote_address");

		if (address == null) {
			address = session.getRemoteSocketAddress().toString();

			session.setAttribute("_remote_address", address);
		}

		return address;
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		msgAdapterThread = new UniqueThread(msgAdapter, "WebSocketChat");

		msgAdapterThread.start();

		super.initialize(context, config);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

		LifeCycleUtil.stop(msgAdapterThread);

		super.destroy(context, config);
	}

	class MsgAdapter implements Looper {

		private List<Session>		clients	= new ArrayList<Session>();

		private LinkedList<String>	msgs		= new LinkedListABQ<String>(1024 * 4);

		public void stop() {

		}

		public synchronized void addClient(Session session) {
				
			clients.add(session);
			
			logger.info("当前客户端数量：{}",clients.size());
		}

		public synchronized void removeClient(Session session) {
				
			clients.remove(session);
			
			logger.info("当前客户端数量：{}",clients.size());
		}

		public void sendMsg(String msg) {
			msgs.offer(msg);
		}

		public void loop() {

			String msg = msgs.poll(16);

			if (msg == null) {
				return;
			}
			
			logger.info("WebSocketMsg:{}",msg);

			synchronized (this) {

				for (Session s : clients) {
					
					if (s.isOpened()) {
						
						WebSocketReadFuture f = new WebSocketReadFutureImpl(s);
						
						f.write(msg);
						
						s.flush(f);
					}else{
						removeClient(s);
					}
				}
			}
		}
	}
}
