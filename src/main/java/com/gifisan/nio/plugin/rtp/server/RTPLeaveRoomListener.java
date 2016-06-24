package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class RTPLeaveRoomListener implements SessionEventListener{
	
	public void sessionOpened(Session session) {
		
		RTPContext context = RTPContextFactory.getRTPContext();
		
		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context);

		if (attachment == null) {

			attachment = new RTPSessionAttachment(context);

			session.setAttachment(context, attachment);
		}

	}

	public void sessionClosed(Session session) {
		
		RTPContext context = RTPContextFactory.getRTPContext();
		
		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context);
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room != null) {
			
			Session _session = (Session) session;
			
			room.leave(_session.getUDPEndPoint());
		}
	}
	
}
