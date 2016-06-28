package com.gifisan.nio.extend.plugin.rtp.server;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.Session;

public class RTPSessionAttachment implements Attachment {

	private RTPContext	context	;

	private RTPRoom	rtpRoom	;

	public RTPRoom getRtpRoom() {
		return rtpRoom;
	}

	public RTPRoom createRTPRoom(Session session) {
		if (rtpRoom == null) {
			
			rtpRoom = new RTPRoom(context,session);
			
			RTPRoomFactory factory = context.getRTPRoomFactory();
			
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
