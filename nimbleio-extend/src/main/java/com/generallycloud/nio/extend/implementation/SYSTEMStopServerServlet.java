package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.acceptor.IOAcceptor;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class SYSTEMStopServerServlet extends NIOFutureAcceptorService {

	public static final String	SERVICE_NAME	= SYSTEMStopServerServlet.class.getSimpleName();

	private Logger				logger		= LoggerFactory.getLogger(SYSTEMStopServerServlet.class);

	public void doAccept(Session session, NIOReadFuture future) throws Exception {
		
		NIOContext context = session.getContext();
		
		new Thread(new StopServer(context)).start();
		
		future.write("服务端正在处理停止服务命令...");
		
		session.flush(future);
	}

	private class StopServer implements Runnable {
		
		private NIOContext context = null;

		public StopServer(NIOContext context) {
			this.context = context;
		}

		public void run() {

			ThreadUtil.sleep(500);

			logger.info("   [NIOServer] 执行命令：<停止服务>");

			String[] words = new String[] { "五", "四", "三", "二", "一" };

			for (int i = 0; i < 5; i++) {

				logger.info("   [NIOServer] 服务将在" + words[i] + "秒后开始停止，请稍等");

				ThreadUtil.sleep(1000);

			}
			
			IOAcceptor tcpAcceptor = (IOAcceptor) context.getTCPService();
			IOAcceptor udpAcceptor = (IOAcceptor) context.getUDPService();
			
			tcpAcceptor.unbind();
			
			if (udpAcceptor!=null) {
				udpAcceptor.unbind();
			}
		}
	}
}
