package com.test.servlet.http;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.Looper;
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

public class TestWebSocketRumpetrollServlet extends HTTPFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketRumpetrollServlet.class);

	private MsgAdapter		msgAdapter	= new MsgAdapter();

	private UniqueThread	msgAdapterThread;
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();
		
		msgAdapter.addClient(session.getIOSession());
		
		session.flush(future);
		
		Session ioSession = session.getIOSession();
		
		JSONObject o = new JSONObject();
		o.put("type", "welcome");
		o.put("id", ioSession.getSessionID());
//		o.put("name", getAddress(ioSession));
		
		WebSocketReadFuture f = new WebSocketReadFutureImpl(session.getIOSession());
		
		f.write(o.toJSONString());
		
		session.flush(f);
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
			
			JSONObject o = new JSONObject();
			o.put("type", "closed");
			o.put("id", session.getSessionID());
			
			msgAdapter.sendMsg(o.toJSONString());
			
			logger.info("客户端主动关闭连接：{}",session);
		} else {

			String msg = f.getData().toString(Encoding.UTF8);
			
			JSONObject o = JSONObject.parseObject(msg);
			
			o.put("name", getAddress(session));
			o.put("id", session.getSessionID());
			o.put("life", "1");
			o.put("authorized", "false");
			o.put("x", Double.valueOf(o.getString("x")));
			o.put("y", Double.valueOf(o.getString("x")));
			o.put("momentum", Double.valueOf(o.getString("momentum")));
			o.put("angle", Double.valueOf(o.getString("angle")));
			
			msgAdapter.sendMsg(o.toJSONString());
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

		msgAdapterThread = new UniqueThread(msgAdapter, "WebSocketRumpetroll");

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
			
			synchronized (this) {

				for (Session s : clients) {
					
					if (s.isOpened()) {
						
						WebSocketReadFuture f = new WebSocketReadFutureImpl(s);
						
						f.write(msg.getBytes(Encoding.UTF8));
						
						s.flush(f);
					}else{
						removeClient(s);
					}
				}
			}
		}
	}
}
