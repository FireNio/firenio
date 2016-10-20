package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public class WebSocketBeatFutureFactory implements BeatFutureFactory {

	public ReadFuture createPINGPacket(Session session) {
		if ("WebSocket".equals(session.getProtocolID())) {
			return new WebSocketBeatReadFutureImpl(session.getContext(),true);
		}
		return null;
	}

	public ReadFuture createPONGPacket(Session session) {
		if ("WebSocket".equals(session.getProtocolID())) {
			return new WebSocketBeatReadFutureImpl(session.getContext(),false);
		}
		return null;
	}

}
