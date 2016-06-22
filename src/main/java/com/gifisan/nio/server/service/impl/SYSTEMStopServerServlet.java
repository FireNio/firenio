package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOAcceptor;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.NIOServlet;

public class SYSTEMStopServerServlet extends NIOServlet {

	public static final String	SERVICE_NAME	= SYSTEMStopServerServlet.class.getSimpleName();

	private Logger				logger		= LoggerFactory.getLogger(SYSTEMStopServerServlet.class);

	public void accept(IOSession session, ReadFuture future) throws Exception {
		
		ServerContext context = (ServerContext) session.getContext();
		
		NIOAcceptor server = context.getServer();
		
		new Thread(new StopServer(server)).start();
		
		future.write("服务端正在处理停止服务命令...");
		
		session.flush(future);
	}

	private class StopServer implements Runnable {

		private NIOAcceptor	server	= null;

		public StopServer(NIOAcceptor server) {
			this.server = server;
		}

		public void run() {

			ThreadUtil.sleep(500);

			logger.info("   [NIOServer] 执行命令：<停止服务>");

			String[] words = new String[] { "五", "四", "三", "二", "一" };

			for (int i = 0; i < 5; i++) {

				logger.info("   [NIOServer] 服务将在" + words[i] + "秒后开始停止，请稍等");

				ThreadUtil.sleep(1000);

			}

			LifeCycleUtil.stop(server);
		}
	}

}
