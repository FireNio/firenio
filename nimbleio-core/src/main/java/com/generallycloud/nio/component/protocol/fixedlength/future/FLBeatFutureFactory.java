package com.generallycloud.nio.component.protocol.fixedlength.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createBeatPacket(Session session) {
		return new FixedLengthReadFutureImpl(session, true);
	}
}
