package com.gifisan.nio.client;

import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.future.ReadFuture;

public interface ProtectedClientSession extends ClientSession {

	public abstract MessageBus getMessageBus();

	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

	public abstract void offer();

	public abstract void offer(ReadFuture future);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);
}
