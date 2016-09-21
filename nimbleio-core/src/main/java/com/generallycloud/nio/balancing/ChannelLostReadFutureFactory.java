package com.generallycloud.nio.balancing;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public interface ChannelLostReadFutureFactory {

	public abstract ReadFuture createChannelLostPacket(Session session);
	
}
