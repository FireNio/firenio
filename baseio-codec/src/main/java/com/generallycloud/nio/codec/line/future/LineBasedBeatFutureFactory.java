package com.generallycloud.nio.codec.line.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public class LineBasedBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createPINGPacket(Session session) {
		throw new UnsupportedOperationException();
	}

	public ReadFuture createPONGPacket(Session session) {
		throw new UnsupportedOperationException();
	}
	
}
