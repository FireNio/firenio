package com.generallycloud.nio.component;

import com.generallycloud.nio.component.protocol.ReadFuture;

public interface BeatFutureFactory {

	public abstract ReadFuture createBeatPacket(Session session);
}
