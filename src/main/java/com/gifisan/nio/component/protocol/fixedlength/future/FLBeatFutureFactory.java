package com.gifisan.nio.component.protocol.fixedlength.future;

import com.gifisan.nio.component.BeatFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createBeatPacket(Session session) {
		return new FixedLengthReadFutureImpl(session, true);
	}
}
