package com.gifisan.nio.server;

public interface InnerRequest extends Request{

	public abstract Request update(ServerEndPoint endPoint);
}
