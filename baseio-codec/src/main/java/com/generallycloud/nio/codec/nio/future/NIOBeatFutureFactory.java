package com.generallycloud.nio.codec.nio.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public class NIOBeatFutureFactory implements BeatFutureFactory {

	public ReadFuture createPINGPacket(Session session) {
		return new NIOReadFutureImpl(session.getContext()).setPING();
	}

	public ReadFuture createPONGPacket(Session session) {
		return new NIOReadFutureImpl(session.getContext()).setPONG();
	}

}
