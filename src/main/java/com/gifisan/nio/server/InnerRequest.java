package com.gifisan.nio.server;

import com.gifisan.nio.component.ServerProtocolData;

public interface InnerRequest extends Request{

	public abstract void update(ServerEndPoint endPoint,ServerProtocolData decoder);
}
