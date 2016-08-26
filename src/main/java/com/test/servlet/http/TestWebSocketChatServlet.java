package com.test.servlet.http;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.DateUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestWebSocketChatServlet extends HTTPFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketChatServlet.class);

	private WebSocketMsgAdapter	msgAdapter	= new WebSocketMsgAdapter();

	private UniqueThread	msgAdapterThread;

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		String token = future.getRequestParam("token");

		if ("WebSocket".equals(token)) {
			
			future.updateWebSocketProtocol();
			
			session.flush(future);
			
			msgAdapter.addClient(session.getIOSession());
			
			String msg = getMsg(session.getIOSession(),"加入房间");
			
			msgAdapter.sendMsg(msg);
			
		} else {
			
			future.write("illegal argement token:" + token);
			
			session.flush(future);
		}
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
			
			logger.info("客户端主动关闭连接：{}", session);
			
			String msg = getMsg(session,"离开房间");
			
			msgAdapter.sendMsg(msg);
		} else {

			String msg = getMsg(session, f.getData().toString(Encoding.UTF8));
			
			msgAdapter.sendMsg(msg);
		}
	}
	
	private String getMsg(Session session,String msg){
		StringBuilder b = new StringBuilder();
		String address = getAddress(session);
		b.append("[");
		b.append(address);
		b.append("][");
		b.append(DateUtil.now());
		b.append("]:");
		b.append(msg);
		return b.toString();
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
}
