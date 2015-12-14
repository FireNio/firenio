package com.gifisan.mtp.servlet.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.MTPServer;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;

public class StopServerServlet extends MTPServlet{
	
	private final Logger logger = LoggerFactory.getLogger(StopServerServlet.class);

	public static final String SERVICE_NAME = "stop-server";
	
	public void accept(Request request, Response response) throws Exception {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StringUtil.isBlankOrNull(username) || StringUtil.isBlankOrNull(password)) {
			return;
		}
		if (password.length() > username.length()) {
			ServletContext context = request.getSession().getServletContext();
			MTPServer server = context.getServer();
			new Thread(new StopServer(server)).start();
			response.write("处理服务器停止命令...".getBytes(context.getEncoding()));
			response.flush();
		}
		
	}
	
	private class StopServer implements Runnable{

		private MTPServer server = null;
		
		public StopServer(MTPServer server) {
			this.server = server;
		}

		public void run() {
			logger.info("[MTPServer] 执行命令：<停止服务>");
			String [] words = new String[]{"三","二","一"};
			for (int i = 0; i < 3; i++) {
				logger.info("[MTPServer] 服务将在"+words[i]+"秒后开始停止，请稍等");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			LifeCycleUtil.stop(server);
		}
	}

}
