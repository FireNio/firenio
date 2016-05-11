package com.gifisan.nio.rtp.server;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.ActiveAuthority;

public class RTPSessionAttachment implements Attachment {

	private RTPContext		context	= null;
	private ActiveAuthority	authority	= null;

	protected RTPSessionAttachment(RTPContext context) {
		this.context = context;
	}
	
	public ActiveAuthority getAuthority() {
		return authority;
	}

	protected void setAuthority(ActiveAuthority authority) {
		this.authority = authority;
	}

}
