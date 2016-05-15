package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class RTPCreateRoomServlet extends RTPServlet{
	
	public static final String SERVICE_NAME = RTPCreateRoomServlet.class.getSimpleName();
	
	public void accept(IOSession session, ServerReadFuture future, RTPSessionAttachment attachment) throws Exception {
		
		RTPContext context = getRTPContext();
		
		if (context.isLogined(session)) {
			
			attachment.createRTPRoom();
			
			future.write(ByteUtil.TRUE);
			
		}else{
			
			future.write(ByteUtil.FALSE);
		}
		
		session.flush(future);
	}
	
}
