package com.gifisan.nio.server;

import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.service.Request;

public interface InnerRequest extends Request{

	public abstract void update(ServerEndPoint endPoint,ProtocolData decoder);
}
