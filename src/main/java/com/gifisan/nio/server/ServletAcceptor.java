package com.gifisan.nio.server;

public interface ServletAcceptor {

	public abstract void accept(Request request, Response response) throws Exception;

}
