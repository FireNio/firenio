package com.gifisan.nio.component;

import com.gifisan.nio.component.protocol.ReadFuture;

public interface BeatFutureFactory {

	public abstract ReadFuture createBeatPacket(Session session);
}
