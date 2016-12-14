package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceReverseSocketSession;

public class Node {

	Node(int index) {
		this.index = index;
	}

	int index;
	
	BalanceReverseSocketSession machine;
}
