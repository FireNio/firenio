package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface ChannelLostReadFutureFactory {

	public abstract ReadFuture createChannelLostPacket(SocketSession session);
	
}
