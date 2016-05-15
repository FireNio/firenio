package com.gifisan.nio.client;

import com.gifisan.nio.component.protocol.DatagramPacket;

public interface OnDatagramPacketReceive {

	public abstract void onDatagramPacketReceived(DatagramPacket packet);
	
}
