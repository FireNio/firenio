package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class RTPLoginServlet extends RTPServlet {
	
	public static final String SERVICE_NAME = RTPLoginServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();

		if (!context.isLogined(session)) {

			LoginCenter loginCenter = context.getLoginCenter();

			Parameters param = future.getParameters();

			if (!loginCenter.login(session, future)) {

				DebugUtil.debug(">>>> {} login failed !", param.getParameter("username"));

				future.write(ByteUtil.FALSE);

				session.flush(future);

				session.disconnect();

				return;
			}
			
			if (attachment == null) {

				attachment = new RTPSessionAttachment(context);

				session.setAttachment(context, attachment);
			}

			DebugUtil.debug(">>>> {} login successful !", param.getParameter("username"));

		}
		
		future.write(ByteUtil.TRUE);

		session.flush(future);
	}


}
