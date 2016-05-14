package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class JMSLoginServlet extends JMSServlet {
	
	public static final String SERVICE_NAME = JMSLoginServlet.class.getSimpleName();
	
	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();
		
		if (attachment == null) {

			attachment = new JMSSessionAttachment(context);

			session.setAttachment(context, attachment);
		}
		
		if (!context.isLogined(session)) {
			
			LoginCenter loginCenter = context.getLoginCenter();
			
			Parameters param = future.getParameters();
			
			if (!loginCenter.login(session, future)) {
				
				DebugUtil.debug(">>>> {} login failed !",param.getParameter("username"));
				
				future.write(ByteUtil.FALSE);
				
				session.flush(future);

				session.disconnect();
				
				return ;
			}
			
			boolean isConsumer = param.getBooleanParameter("consumer");

			if (isConsumer) {
				session.addEventListener(new TransactionProtectListener(context));

				String queueName = param.getParameter("queueName");

				attachment.addQueueName(queueName);

				context.addReceiver(queueName);
			}

			DebugUtil.debug(">>>> {} login successful !",param.getParameter("username"));
			
		}
		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
	
}
