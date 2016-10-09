package com.generallycloud.nio.codec.fixedlength.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createPINGPacket(Session session) {
		return new FixedLengthReadFutureImpl().setPING();
	}

	public ReadFuture createPONGPacket(Session session) {
		return new FixedLengthReadFutureImpl().setPONG();
	}
	
}
