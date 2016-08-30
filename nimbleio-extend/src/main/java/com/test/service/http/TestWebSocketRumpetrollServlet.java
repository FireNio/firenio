package com.test.service.http;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFutureImpl;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestWebSocketRumpetrollServlet extends HTTPFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketRumpetrollServlet.class);

	private WebSocketMsgAdapter	msgAdapter	= new WebSocketMsgAdapter();

	private UniqueThread	msgAdapterThread;

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();
		
		session.flush(future);

		msgAdapter.addClient(session.getIOSession());

		Session ioSession = session.getIOSession();

		JSONObject o = new JSONObject();
		o.put("type", "welcome");
		o.put("id", ioSession.getSessionID());

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

			logger.info("客户端主动关闭连接：{}", session);
		} else {

			String msg = f.getData().toString(Encoding.UTF8);

			JSONObject o = JSONObject.parseObject(msg);

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
}
