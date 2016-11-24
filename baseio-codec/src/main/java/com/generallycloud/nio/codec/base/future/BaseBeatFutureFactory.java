package com.generallycloud.nio.codec.base.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BaseBeatFutureFactory implements BeatFutureFactory {

	public ReadFuture createPINGPacket(SocketSession session) {
		return new BaseReadFutureImpl(session.getContext()).setPING();
	}

	public ReadFuture createPONGPacket(SocketSession session) {
		return new BaseReadFutureImpl(session.getContext()).setPONG();
	}

}
