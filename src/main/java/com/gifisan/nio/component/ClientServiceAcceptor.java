package com.gifisan.nio.component;

import com.gifisan.nio.client.ClientContext;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.ServiceAcceptor;

public class ClientServiceAcceptor implements ServiceAcceptor {


	public void accept(Session session, ReadFuture future) {
		ClientSession clientSesssion = (ClientSession) session;
		
		ClientContext context = clientSesssion.getContext();
		//FIXME ...
		
	}


}
