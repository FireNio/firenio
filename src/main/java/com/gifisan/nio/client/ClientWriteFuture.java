package com.gifisan.nio.client;

import com.gifisan.nio.service.WriteFuture;

public interface ClientWriteFuture extends WriteFuture{

	public abstract ClientSesssion getSession();
	
}
