package com.generallycloud.nio.balancing.router;

import com.generallycloud.nio.component.IOSession;

public class Machine {
	
	Machine(IOSession session) {
		this.session = session;
		this.session.setAttachment(this);
	}

	IOSession session;
}
