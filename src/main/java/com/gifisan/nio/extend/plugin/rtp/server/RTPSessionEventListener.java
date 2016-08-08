package com.gifisan.nio.extend.plugin.rtp.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class RTPSessionEventListener implements SessionEventListener{
	
	public void sessionOpened(Session session) {
		
		RTPContext context = RTPContext.getInstance();
		
		RTPSessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new RTPSessionAttachment(context);

			session.setAttachment(context, attachment);
		}

	}

	public void sessionClosed(Session session) {
		
		RTPContext context = RTPContext.getInstance();
		
		RTPSessionAttachment attachment = context.getSessionAttachment(session);
		
		if (attachment == null) {
			return;
		}
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room == null) {
			return;
		}

		room.leave(session.getUDPEndPoint());
	}
	
}
