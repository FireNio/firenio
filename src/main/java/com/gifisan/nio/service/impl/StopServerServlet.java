package com.gifisan.nio.service.impl;


import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.NIOServer;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class StopServerServlet extends NIOServlet {

	private Logger				logger		= LoggerFactory.getLogger(StopServerServlet.class);
	private String				username		= null;
	private String				password		= null;
	
	public void accept(IOSession session,ReadFuture future) throws Exception {
		Parameters param = future.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");
		
		boolean result = this.username.equals(username) && this.password.equals(password);
		if (result) {
			ServerContext context = (ServerContext) session.getContext();
			NIOServer server = context.getServer();
			new Thread(new StopServer(server)).start();
			session.write("服务端正在处理停止服务命令...");
		}else{
			session.write(RESMessage.R_UNAUTH.toString());
		}
		session.flush(future);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("username");
		this.password = config.getProperty("password");
	}

	private class StopServer implements Runnable {

		private NIOServer	server	= null;

		public StopServer(NIOServer server) {
			this.server = server;
		}

		public void run() {
			
			ThreadUtil.sleep(500);
			
			logger.info("  [NIOServer] 执行命令：<停止服务>");
			
			String[] words = new String[] { "五", "四", "三", "二", "一" };
			
			for (int i = 0; i < 5; i++) {
				
				logger.info("  [NIOServer] 服务将在" + words[i] + "秒后开始停止，请稍等");
				
				ThreadUtil.sleep(1000);
				
			}

			LifeCycleUtil.stop(server);
		}
	}

}
