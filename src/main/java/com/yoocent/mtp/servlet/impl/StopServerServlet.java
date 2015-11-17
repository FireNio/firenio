package com.yoocent.mtp.servlet.impl;

import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.MTPServer;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;

public class StopServerServlet extends MTPServlet{
	
	public static final String SERVICE_KEY = "stop-server";

	public void accept(Request request, Response response) throws Exception {
		String username = request.getStringParameter("username");
		String password = request.getStringParameter("password");
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
			System.out.println("[MTPServer] 执行命令：<停止服务>");
			String [] words = new String[]{"三","二","一"};
			for (int i = 0; i < 3; i++) {
				System.out.println("[MTPServer] 服务将在"+words[i]+"秒后开始停止，请稍等");
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
