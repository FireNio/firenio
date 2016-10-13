package com.generallycloud.nio.extend;

import java.util.HashSet;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;

public class IPFilter extends SEListenerAdapter{

	private HashSet<String> blackIPs;
	
	public IPFilter(HashSet<String> blackIPs) {
		this.blackIPs = blackIPs;
	}

	public void sessionOpened(Session session) {
		if (!blackIPs.contains(session.getRemoteAddr())) {
			CloseUtil.close(session);
		}
		super.sessionOpened(session);
	}
	
}
