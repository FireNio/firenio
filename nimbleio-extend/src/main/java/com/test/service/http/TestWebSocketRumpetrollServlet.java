package com.test.service.http;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestWebSocketRumpetrollServlet extends HTTPFutureAcceptorService {

	private Logger			logger		= LoggerFactory.getLogger(TestWebSocketRumpetrollServlet.class);

	private WebSocketMsgAdapter	msgAdapter	= new WebSocketMsgAdapter();

	private EventLoopThread	msgAdapterThread;

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();
		
		session.flush(future);

		msgAdapter.addClient(session.getIOSession());

		Session ioSession = session.getIOSession();

		JSONObject o = new JSONObject();
		o.put("type", "welcome");
		o.put("id", ioSession.getSessionID());

		WebSocketReadFuture f = new WebSocketTextReadFutureImpl();

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

		msgAdapterThread = new EventLoopThread(msgAdapter, "WebSocketRumpetroll");

		msgAdapterThread.start();

		super.initialize(context, config);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

		LifeCycleUtil.stop(msgAdapterThread);

		super.destroy(context, config);
	}
}
