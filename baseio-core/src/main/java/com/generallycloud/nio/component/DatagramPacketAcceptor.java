package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.protocol.DatagramPacket;

public interface DatagramPacketAcceptor {
	
	public abstract void accept(DatagramSession session,DatagramPacket packet) throws IOException;
}
