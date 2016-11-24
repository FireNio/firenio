package com.generallycloud.nio.extend;

import java.util.HashSet;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class IPFilter extends SEListenerAdapter{

	private HashSet<String> blackIPs;
	
	public IPFilter(HashSet<String> blackIPs) {
		this.blackIPs = blackIPs;
	}

	public void sessionOpened(SocketSession session) {
		if (!blackIPs.contains(session.getRemoteAddr())) {
			CloseUtil.close(session);
		}
	}
	
}
