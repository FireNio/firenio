package com.generallycloud.nio.component.protocol.nio.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class NIOBeatFutureFactory implements BeatFutureFactory {

	public ReadFuture createPINGPacket(Session session) {
		return new NIOReadFutureImpl(session).setPING();
	}

	public ReadFuture createPONGPacket(Session session) {
		return new NIOReadFutureImpl(session).setPONG();
	}

}
