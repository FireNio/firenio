package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceReverseSocketSession;

public class Machine {
	
	Machine(BalanceReverseSocketSession session) {
		this.session = session;
		this.session.setAttachment(this);
	}

	BalanceReverseSocketSession session;
}
