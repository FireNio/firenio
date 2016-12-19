package com.generallycloud.nio.codec.fixedlength.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	@Override
	public ReadFuture createPINGPacket(SocketSession session) {
		return new FixedLengthReadFutureImpl(session.getContext()).setPING();
	}

	@Override
	public ReadFuture createPONGPacket(SocketSession session) {
		return new FixedLengthReadFutureImpl(session.getContext()).setPONG();
	}
	
}
