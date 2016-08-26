package com.gifisan.nio.component.protocol.nio.future;

import com.gifisan.nio.component.BeatFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;

public class NIOBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createBeatPacket(Session session) {
		return new NIOBeatReadFuture(session);
	}
}
