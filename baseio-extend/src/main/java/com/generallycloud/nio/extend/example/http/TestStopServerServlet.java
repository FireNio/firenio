package com.generallycloud.nio.extend.example.http;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;

public class TestStopServerServlet extends HTTPFutureAcceptorService {

	private Logger				logger		= LoggerFactory.getLogger(TestStopServerServlet.class);

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		BaseContext context = session.getIoSession().getContext();
		
		new Thread(new StopServer(context)).start();
		
		future.write("服务端正在处理停止服务命令...");
		
		session.flush(future);
		
	}

	private class StopServer implements Runnable {
		
		private BaseContext context = null;

		public StopServer(BaseContext context) {
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
			
			ChannelAcceptor acceptor = (ChannelAcceptor) context.getChannelService();
			
			acceptor.unbind();
			
		}
	}
}
