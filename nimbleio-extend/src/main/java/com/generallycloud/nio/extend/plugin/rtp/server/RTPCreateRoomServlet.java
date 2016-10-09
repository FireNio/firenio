package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.component.Session;

public class RTPCreateRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPCreateRoomServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.createRTPRoom(session);

		future.write(String.valueOf(room.getRoomID()));

		session.flush(future);
	}

}
