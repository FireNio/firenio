package com.gifisan.nio.server;

import com.gifisan.nio.component.ProtocolData;

public interface InnerRequest extends Request{

	public abstract void update(ServerEndPoint endPoint,ProtocolData decoder);
}
