package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class UnsafeDatagramSessionImpl extends DatagramSessionImpl implements UnsafeDatagramSession{

	private static final Logger logger = LoggerFactory.getLogger(UnsafeDatagramSessionImpl.class);
	
	public UnsafeDatagramSessionImpl(DatagramChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public DatagramChannel getDatagramChannel() {
		return channel;
	}

	public void fireOpend() {
		
		Linkable<DatagramSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionOpened(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
		
	}

	public void fireClosed() {
		
		Linkable<DatagramSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionClosed(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
		
	}

	public void physicalClose() {
		
		fireClosed();
	}
	
}
