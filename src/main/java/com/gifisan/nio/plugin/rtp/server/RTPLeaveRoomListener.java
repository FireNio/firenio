package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;
import com.gifisan.nio.server.IOSession;

public class RTPLeaveRoomListener implements SessionEventListener{
	
	private RTPContext context = null;

	protected RTPLeaveRoomListener(RTPContext context) {
		this.context = context;
	}

	public void onDestroy(Session session) {
		
		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context);
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			
			IOSession _session = (IOSession) session;
			
			room.leave(_session.getUDPEndPoint());
		}
	}
}
