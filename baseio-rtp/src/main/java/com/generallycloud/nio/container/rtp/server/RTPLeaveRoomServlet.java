package com.generallycloud.nio.container.rtp.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.SocketSession;

public class RTPLeaveRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPLeaveRoomServlet.class.getSimpleName();

	public void doAccept(SocketSession session, BaseReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
//			room.leave(session.getDatagramChannel()); //FIXME udp 
		}

		future.write(ByteUtil.TRUE);
		
		session.flush(future);
	}
}
