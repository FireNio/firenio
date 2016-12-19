package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.codec.http11.WebSocketProtocolFactory;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class WebSocketBeatFutureFactory implements BeatFutureFactory {

	@Override
	public ReadFuture createPINGPacket(SocketSession session) {
		if (WebSocketProtocolFactory.PROTOCOL_ID.equals(session.getProtocolID())) {
			return new WebSocketBeatReadFutureImpl(session.getContext(),true);
		}
		return null;
	}

	@Override
	public ReadFuture createPONGPacket(SocketSession session) {
		if (WebSocketProtocolFactory.PROTOCOL_ID.equals(session.getProtocolID())) {
			return new WebSocketBeatReadFutureImpl(session.getContext(),false);
		}
		return null;
	}

}
