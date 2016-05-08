package com.gifisan.nio.jms.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;

public class JMSLoginServlet extends JMSServlet {

	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();
		
		if (attachment == null) {

			attachment = new JMSSessionAttachment(context);

			session.attach(attachment);
		}
		
		Authority authority = attachment.getAuthority();
		
		if (authority == null || !authority.isAuthored()) {
			
			LoginCenter loginCenter = context.getLoginCenter();
			
			authority = loginCenter.login(session, future);
			
			Parameters param = future.getParameters();
			
			if (!loginCenter.logined(session, future)) {
				
				DebugUtil.debug(">>>> {} login failed !",param.getParameter("username"));
				
				future.write(ByteUtil.FALSE);
				
				session.flush(future);

				session.disconnect();
				
				return ;
			}
			
			boolean isConsumer = param.getBooleanParameter("consumer");

			if (isConsumer) {
				session.addEventListener(new TransactionProtectListener());

				String queueName = param.getParameter("queueName");

				attachment.addQueueName(queueName);

				context.addReceiver(queueName);
			}

			DebugUtil.debug(">>>> {} login successful !",param.getParameter("username"));
			
		}
		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {

		MQContext mqContext = getMQContext();
		
		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
		
		mqContext.reload();
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {

		MQContextFactory.setNullMQContext();
		
		super.destroy(context, config);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		MQContext mqContext = getMQContext();
		
		long dueTime = config.getLongProperty("due-time");

		mqContext.setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);
		
		MQContextFactory.initializeContext();
		
		super.initialize(context, config);
	}

}
