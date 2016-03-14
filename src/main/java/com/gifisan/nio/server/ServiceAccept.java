package com.gifisan.nio.server;

public interface ServiceAccept {

	public abstract void accept(Request request, Response response) throws Exception;

}
