package com.gifisan.nio.client;

import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.security.Authority;

public interface ProtectedClientSession extends ClientSession {

	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);
	
	public abstract void offerReadFuture(IOReadFuture future);
	
	public abstract void setSessionID(String sessionID);
	
	public abstract void setAuthority(Authority authority);
	
	public abstract void setMachineType(String machineType);
}
