package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class RTPLoginServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPLoginServlet.class.getSimpleName();

	private Logger				logger		= LoggerFactory.getLogger(RTPLoginServlet.class);

	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();

		if (!context.isLogined(session)) {

			LoginCenter loginCenter = context.getLoginCenter();

			Parameters param = future.getParameters();

			if (!loginCenter.login(session, future)) {

				logger.debug(">>>> {} login failed !", param.getParameter("username"));

				future.write(ByteUtil.FALSE);

				session.flush(future);

				session.disconnect();

				return;
			}

			if (attachment == null) {

				attachment = new RTPSessionAttachment(context);

				session.setAttachment(context, attachment);
			}

			logger.debug(">>>> {} login successful !", param.getParameter("username"));

		}

		future.write(ByteUtil.TRUE);

		session.flush(future);
	}

}
