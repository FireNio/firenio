package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class RTPLeaveRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPLeaveRoomServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.leave(session.getUDPEndPoint());
		}

		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
}
