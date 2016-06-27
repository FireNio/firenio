package com.gifisan.nio.front;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Session;

public class RouterProxy {

	private ArrayList<Session>		list					= new ArrayList<Session>();
	private int					index				= 0;
	private HashMap<String, Session>	routerMapping			= new HashMap<String, Session>();
	private final String			REMOTE_SOCKET_ADDRESS	= "_REMOTE_SOCKET_ADDRESS";
	private Logger					logger				= LoggerFactory.getLogger(RouterProxy.class);

	public synchronized Session getNext() {

		if (list.isEmpty()) {
			return null;
		}

		if (index < list.size()) {
			return list.get(index++);
		} else {
			index = 0;
			return list.get(index);
		}
	}

	public synchronized void addSession(Session session) {
		this.list.add(session);
	}

	public synchronized void remove(Session session) {
		list.remove(session);
	}

	public Session getSession(Session session) {

		String address = (String) session.getAttribute(REMOTE_SOCKET_ADDRESS);
		if (StringUtil.isNullOrBlank(address)) {
			address = session.getRemoteSocketAddress().toString();
			session.setAttribute(REMOTE_SOCKET_ADDRESS, address);
		}

		Session _session = routerMapping.get(address);

		if (_session == null) {

			return getSession(address);
		}

		if (!_session.isOpened()) {
			synchronized (this) {
				routerMapping.remove(address);
			}

			return getSession(address);

		}

		return _session;
	}

	private Session getSession(String address) {

		Session _session = getNext();

		if (_session == null) {
			return null;
		}

		synchronized (this) {
			routerMapping.put(address, _session);
		}

		return _session;
	}

}
