package com.generallycloud.nio.component;

import com.generallycloud.nio.component.protocol.ReadFuture;

public interface BeatFutureFactory {

	public abstract ReadFuture createPINGPacket(Session session);
	
	public abstract ReadFuture createPONGPacket(Session session);
}
