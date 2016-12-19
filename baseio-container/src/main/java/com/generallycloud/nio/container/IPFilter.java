package com.generallycloud.nio.container;

import java.util.HashSet;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;

public class IPFilter extends SocketSEListenerAdapter{

	private HashSet<String> blackIPs;
	
	public IPFilter(HashSet<String> blackIPs) {
		this.blackIPs = blackIPs;
	}

	@Override
	public void sessionOpened(SocketSession session) {
		if (!blackIPs.contains(session.getRemoteAddr())) {
			CloseUtil.close(session);
		}
	}
	
}
