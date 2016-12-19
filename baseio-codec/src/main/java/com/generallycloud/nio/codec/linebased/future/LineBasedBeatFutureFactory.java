package com.generallycloud.nio.codec.linebased.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class LineBasedBeatFutureFactory implements BeatFutureFactory{

	@Override
	public ReadFuture createPINGPacket(SocketSession session) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReadFuture createPONGPacket(SocketSession session) {
		throw new UnsupportedOperationException();
	}
	
}
