package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public interface ChannelLostReadFutureFactory {

	public abstract ReadFuture createChannelLostPacket(Session session);
	
}
