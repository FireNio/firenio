package com.gifisan.nio.client.session;

import com.gifisan.nio.client.ClientSesssion;

public interface ClientSessionFactory {

	public abstract ClientSesssion getClientSesssion();

	public abstract int getSessionSize();

}
