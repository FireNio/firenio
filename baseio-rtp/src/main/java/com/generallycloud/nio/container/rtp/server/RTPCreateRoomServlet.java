package com.generallycloud.nio.container.rtp.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;

public class RTPCreateRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPCreateRoomServlet.class.getSimpleName();

	public void doAccept(SocketSession session, ProtobaseReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.createRTPRoom(session);

		future.write(String.valueOf(room.getRoomID()));

		session.flush(future);
	}

}
