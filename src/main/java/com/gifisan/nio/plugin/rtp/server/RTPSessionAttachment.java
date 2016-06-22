package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.Session;

public class RTPSessionAttachment implements Attachment {

	private RTPContext	context	= null;

	private RTPRoom	rtpRoom	= null;

	public RTPRoom getRtpRoom() {
		return rtpRoom;
	}

	public RTPRoom createRTPRoom(Session session) {
		if (rtpRoom == null) {
			
			rtpRoom = new RTPRoom(context,session);
			
			RTPRoomFactory factory = context.getRTPRoomFactory();
			
			session.addEventListener(new RTPLeaveRoomListener(context));
			
			factory.putRTPRoom(rtpRoom);
		}
		return rtpRoom;
	}

	protected RTPSessionAttachment(RTPContext context) {
		this.context = context;
	}
	
	protected void setRTPRoom(RTPRoom room){
		this.rtpRoom = room;
	}

}
