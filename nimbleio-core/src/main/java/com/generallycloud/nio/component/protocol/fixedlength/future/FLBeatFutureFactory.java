package com.generallycloud.nio.component.protocol.fixedlength.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createPINGPacket(Session session) {
		return new FixedLengthReadFutureImpl(session).setPING();
	}

	public ReadFuture createPONGPacket(Session session) {
		return new FixedLengthReadFutureImpl(session).setPONG();
	}
	
}
