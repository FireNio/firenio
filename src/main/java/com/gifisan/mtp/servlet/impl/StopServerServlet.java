package com.gifisan.mtp.servlet.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.common.DebugUtil;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.server.MTPServer;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class StopServerServlet extends MTPServlet {

	private Logger				logger		= LoggerFactory.getLogger(StopServerServlet.class);
	private String				username		= null;
	private String				password		= null;
	
	public void accept(Request request, Response response) throws Exception {
		RequestParam param = request.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");
		
		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
			ServerContext context = request.getSession().getServerContext();
			MTPServer server = context.getServer();
			new Thread(new StopServer(server)).start();
			response.write("处理服务器停止命令...");
		}else{
			response.write(RESMessage.R_UNAUTH.toString());
		}
		response.flush();
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}

	private class StopServer implements Runnable {

		private MTPServer	server	= null;

		public StopServer(MTPServer server) {
			this.server = server;
		}

		public void run() {
			logger.info("[MTPServer] 执行命令：<停止服务>");
			String[] words = new String[] { "五", "四", "三", "二", "一" };
			for (int i = 0; i < 5; i++) {
				logger.info("[MTPServer] 服务将在" + words[i] + "秒后开始停止，请稍等");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					DebugUtil.debug(e);
				}
			}

			LifeCycleUtil.stop(server);
		}
	}

}
