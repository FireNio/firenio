package com.generallycloud.nio.codec.line.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class LineBasedBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createPINGPacket(SocketSession session) {
		throw new UnsupportedOperationException();
	}

	public ReadFuture createPONGPacket(SocketSession session) {
		throw new UnsupportedOperationException();
	}
	
}
