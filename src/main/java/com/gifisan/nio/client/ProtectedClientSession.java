package com.gifisan.nio.client;

import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.future.IOReadFuture;

public interface ProtectedClientSession extends ClientSession {

	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);
	
	public abstract void offerReadFuture(IOReadFuture future);
}
