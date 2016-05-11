package com.gifisan.nio.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;

public class RTPLoginServlet extends JMSServlet {

	public void accept(IOSession session,ServerReadFuture future,RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();
		
		if (attachment == null) {

			attachment = new RTPSessionAttachment(context);

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

			DebugUtil.debug(">>>> {} login successful !",param.getParameter("username"));
			
		}
		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {

		RTPContext RTPContext = getRTPContext();
		
		RTPContext.reload();
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {

		RTPContextFactory.setNullRTPContext();
		
		super.destroy(context, config);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		RTPContextFactory.initializeContext();
		
		super.initialize(context, config);
	}

}
