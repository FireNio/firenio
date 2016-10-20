package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface BeatFutureFactory {

	public abstract ReadFuture createPINGPacket(Session session);
	
	public abstract ReadFuture createPONGPacket(Session session);
}
