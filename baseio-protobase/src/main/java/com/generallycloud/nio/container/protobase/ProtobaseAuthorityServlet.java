package com.generallycloud.nio.container.protobase;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.authority.Authority;
import com.generallycloud.nio.container.authority.SYSTEMAuthorityServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public class ProtobaseAuthorityServlet extends SYSTEMAuthorityServlet{

	protected String getUsername(ReadFuture future) {
		return unwrap(future).getParameters().getParameter("username");
	}

	protected String getPassword(ReadFuture future) {
		return unwrap(future).getParameters().getParameter("password");
	}

	protected void writeRusult(ReadFuture future, Authority authority) {
		RESMessage message = new RESMessage(0, authority,null);
		unwrap(future).write(message.toString());
	}
	
	private BaseReadFuture unwrap(ReadFuture future){
		return (BaseReadFuture)future;
	}

}
