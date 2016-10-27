package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.SocketSession;

public class Machine {
	
	Machine(SocketSession session) {
		this.session = session;
		this.session.setAttachment(this);
	}

	SocketSession session;
}
