package com.gifisan.nio.server.service.impl;


import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOServer;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.NIOServlet;

public class StopServerServlet extends NIOServlet {

	private Logger				logger		= LoggerFactory.getLogger(StopServerServlet.class);
	
	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		if (session.getLoginCenter().validate(session, future)) {
			ServerContext context = (ServerContext) session.getContext();
			NIOServer server = context.getServer();
			new Thread(new StopServer(server)).start();
			future.write("服务端正在处理停止服务命令...");
		}else{
			future.write(RESMessage.R_UNAUTH.toString());
		}
		session.flush(future);
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
