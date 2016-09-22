package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class RTPJoinRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPJoinRoomServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();

		Integer roomID = Integer.valueOf(future.getText());

		RTPRoomFactory roomFactory = context.getRTPRoomFactory();

		RTPRoom room = roomFactory.getRTPRoom(roomID);

		DatagramChannel datagramChannel = session.getDatagramChannel();

		if (room == null || datagramChannel == null) {

			future.write(ByteUtil.FALSE);

			session.flush(future);
			
			return;
		}

		if (room.join(datagramChannel)) {
			
			future.write(ByteUtil.TRUE);
			
		} else {

			future.write(ByteUtil.FALSE);
		}

		session.flush(future);

	}

}
