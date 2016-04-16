package com.gifisan.nio.client.session;

import com.gifisan.nio.client.ClientSesssion;

public class UniqueClientSessionFactory implements ClientSessionFactory {

	private ClientSesssion	sesssion	= null;
	
	public UniqueClientSessionFactory(ClientSesssion sesssion) {
		this.sesssion = sesssion;
	}

	public ClientSesssion getClientSesssion() {
		return sesssion;
	}

	public int getSessionSize() {
		return 1;
	}

}
