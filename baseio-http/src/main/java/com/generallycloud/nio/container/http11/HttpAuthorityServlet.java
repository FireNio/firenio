package com.generallycloud.nio.container.http11;

import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.authority.Authority;
import com.generallycloud.nio.container.authority.SYSTEMAuthorityServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpAuthorityServlet extends SYSTEMAuthorityServlet{

	protected String getUsername(ReadFuture future) {
		return unwrap(future).getRequestParam("username");
	}

	protected String getPassword(ReadFuture future) {
		return unwrap(future).getRequestParam("password");
	}

	protected void writeRusult(ReadFuture future, Authority authority) {
		RESMessage message = new RESMessage(0, authority,null);
		unwrap(future).write(message.toString());
	}
	
	private HttpReadFuture unwrap(ReadFuture future){
		return (HttpReadFuture)future;
	}

}
