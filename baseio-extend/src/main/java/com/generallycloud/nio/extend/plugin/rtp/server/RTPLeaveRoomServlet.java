package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.Session;

public class RTPLeaveRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPLeaveRoomServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			room.leave(session.getDatagramChannel());
		}

		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
}
