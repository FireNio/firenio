package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class RTPCreateRoomServlet extends RTPServlet{
	
	public static final String SERVICE_NAME = RTPCreateRoomServlet.class.getSimpleName();
	
	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {
		
		RTPContext context = getRTPContext();
		
		if (context.isLogined(session)) {
			
			RTPRoom room = attachment.createRTPRoom();
			
			Integer roomID = room.getRoomID();
			
			future.write(roomID.toString());
			
		}else{
			
			future.write(String.valueOf(-1));
		}
		
		session.flush(future);
	}
	
}
