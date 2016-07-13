package com.gifisan.nio.extend.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class RTPLeaveRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPLeaveRoomServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.leave(session.getUDPEndPoint());
		}

		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
}
