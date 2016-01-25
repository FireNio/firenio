package com.gifisan.mtp.server;

public interface InnerRequest extends Request{

	public abstract Request update(ServerEndPoint endPoint);
}
