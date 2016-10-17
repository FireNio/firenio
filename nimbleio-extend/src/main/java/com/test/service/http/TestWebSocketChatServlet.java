package com.test.service.http;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFutureImpl;
import com.generallycloud.nio.codec.http11.future.WebSocketTextReadFutureImpl;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestWebSocketChatServlet extends HTTPFutureAcceptorService {

	private WebSocketMsgAdapter	msgAdapter	= new WebSocketMsgAdapter();

	private EventLoopThread	msgAdapterThread;

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {

		future.updateWebSocketProtocol();
		
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
			
			JSONObject obj = new JSONObject();
			
			obj.put("username", session.getAttribute("username"));
			obj.put("numUsers", msgAdapter.getClientSize());
			obj.put("action", "user-left");
			
			String msg1 = obj.toJSONString();
			
			msgAdapter.sendMsg(msg1);
			
		} else {

//			String msg = getMsg(session, );
			
			String msg = f.getData().toString(Encoding.UTF8);
			
			JSONObject obj = JSONObject.parseObject(msg);
			
			String action = obj.getString("action");
			
			if("new-message".equals(action)){
				
				obj.put("username", session.getAttribute("username"));
				
				String msg1 = obj.toJSONString();
				
				msgAdapter.sendMsg(msg1);
				
			}else if("add-user".equals(action)){
				
				msgAdapter.addClient(session);
				
				String username = (String)session.getAttribute("username");
				
				if(username != null){
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
				
			}else if("typing".equals(action)){
				
				obj.put("username", session.getAttribute("username"));
				
				String msg1 = obj.toJSONString();
				
				msgAdapter.sendMsg(msg1);
				
				
			}else if("stop-typing".equals(action)){
				
				obj.put("username", session.getAttribute("username"));
				
				String msg1 = obj.toJSONString();
				
				msgAdapter.sendMsg(msg1);
				
			}else if("disconnect".equals(action)){
				
				msgAdapter.removeClient(session);
				
				obj.put("username", session.getAttribute("username"));
				obj.put("numUsers", msgAdapter.getClientSize());
				obj.put("action", "user-left");
				
				String msg1 = obj.toJSONString();
				
				msgAdapter.sendMsg(msg1);
			}else{
				
				f.write("no action matched:"+action);
				
				session.flush(f);
			}
		}
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		msgAdapterThread = new EventLoopThread(msgAdapter, "WebSocketChat");

		msgAdapterThread.start();

		super.initialize(context, config);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

		LifeCycleUtil.stop(msgAdapterThread);

		super.destroy(context, config);
	}
}
