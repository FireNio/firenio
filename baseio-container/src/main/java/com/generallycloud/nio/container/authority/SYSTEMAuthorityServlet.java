package com.generallycloud.nio.container.authority;

import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.ApplicationContextUtil;
import com.generallycloud.nio.container.LoginCenter;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ParametersReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class SYSTEMAuthorityServlet extends FutureAcceptorService {

	public void accept(SocketSession session, ReadFuture future) throws Exception {

		LoginCenter loginCenter = ApplicationContext.getInstance().getLoginCenter();

		ParametersReadFuture f = (ParametersReadFuture) future;

		Parameters parameters = f.getParameters();

		String username = parameters.getParameter("username");
		String password = parameters.getParameter("password");

		boolean login = loginCenter.login(session, username, password);

		RESMessage message = RESMessage.UNAUTH;

		if (login) {

			Authority authority = ApplicationContextUtil.getAuthority(session);

			message = new RESMessage(0, authority, null);
		}

		future.write(message.toString());

		session.flush(future);
	}

}
