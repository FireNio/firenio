package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface BeatFutureFactory {

	public abstract ReadFuture createPINGPacket(SocketSession session);
	
	public abstract ReadFuture createPONGPacket(SocketSession session);
}
