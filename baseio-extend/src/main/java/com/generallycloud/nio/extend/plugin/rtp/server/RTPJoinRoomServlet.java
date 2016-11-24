package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.DatagramChannel;

public class RTPJoinRoomServlet extends RTPServlet {

	public static final String	SERVICE_NAME	= RTPJoinRoomServlet.class.getSimpleName();

	public void doAccept(SocketSession session, BaseReadFuture future, RTPSessionAttachment attachment) throws Exception {

		RTPContext context = getRTPContext();

		Integer roomID = Integer.valueOf(future.getReadText());

		RTPRoomFactory roomFactory = context.getRTPRoomFactory();

		RTPRoom room = roomFactory.getRTPRoom(roomID);

//		DatagramChannel datagramChannel = session.getDatagramChannel();
		
		//FIXME udp 
		DatagramChannel datagramChannel = null;

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
