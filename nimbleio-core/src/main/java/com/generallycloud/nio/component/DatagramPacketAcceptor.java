package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.component.protocol.DatagramPacket;

public interface DatagramPacketAcceptor {
	
	public abstract void accept(DatagramChannel channel,DatagramPacket packet) throws IOException;
}
