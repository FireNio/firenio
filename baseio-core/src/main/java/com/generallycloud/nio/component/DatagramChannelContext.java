package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.DatagramSessionManager.DatagramSessionManagerEvent;

public interface DatagramChannelContext extends ChannelContext {
	
	public abstract void setSessionManager(DatagramSessionManager sessionManager) ;
	
	@Override
	public abstract DatagramSessionManager getSessionManager();

	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);

	public abstract Linkable<DatagramSessionEventListener> getSessionEventListenerLink();
	
	public abstract void addSessionEventListener(DatagramSessionEventListener listener);
	
	public abstract void offerSessionMEvent(DatagramSessionManagerEvent event);
	
	
}