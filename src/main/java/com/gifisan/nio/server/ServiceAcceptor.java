package com.gifisan.nio.server;

public interface ServiceAcceptor {

	public abstract void accept(Request request, Response response) throws Exception;

}
