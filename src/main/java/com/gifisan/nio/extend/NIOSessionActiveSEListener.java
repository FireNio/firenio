package com.gifisan.nio.extend;

import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.extend.implementation.SYSTEMBeatPacketServlet;

public class NIOSessionActiveSEListener extends SessionActiveSEListener {

	protected ReadFuture getBeatPacket(Session session) {
		
		return ReadFutureFactory.create(session, SYSTEMBeatPacketServlet.SERVICE_NAME);
	}
}
