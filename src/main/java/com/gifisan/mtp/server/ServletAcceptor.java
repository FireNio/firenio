package com.gifisan.mtp.server;

public interface ServletAcceptor {

	public abstract void accept(Request request, Response response) throws Exception;

}
