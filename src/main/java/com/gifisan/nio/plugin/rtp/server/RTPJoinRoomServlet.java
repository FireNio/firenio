package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.test.servlet.TestExceptionServlet;

public class RTPJoinRoomServlet extends RTPServlet{
	
	public static final String SERVICE_NAME = RTPJoinRoomServlet.class.getSimpleName();
	
	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {
		
		RTPContext context = getRTPContext();
		
		if (context.isLogined(session)) {
			
			Parameters parameters = future.getParameters();

			int roomID = parameters.getIntegerParameter("roomID");
			
			RTPRoomFactory roomFactory = context.getRTPRoomFactory();
			
			RTPRoom room = roomFactory.getRTPRoom(roomID);
			
			UDPEndPoint udpEndPoint = session.getUDPEndPoint();
			
			if (room == null || udpEndPoint == null) {
				
				future.write(ByteUtil.FALSE);
				
				session.flush(future);
				return;
			}
			
			if(room.join(udpEndPoint)){
				
				future.write(ByteUtil.TRUE);
			}else{
				
				future.write(ByteUtil.FALSE);
			}

			session.flush(future);
		}else{
			
			future.write(ByteUtil.FALSE);
		}
		
		session.flush(future);
	}
	
}
