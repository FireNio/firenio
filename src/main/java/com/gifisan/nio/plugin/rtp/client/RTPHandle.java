package com.gifisan.nio.plugin.rtp.client;

import com.gifisan.nio.component.protocol.DatagramPacketGroup;
import com.gifisan.nio.plugin.jms.MapMessage;

public abstract class RTPHandle {
	
	public abstract void onReceiveUDPPacket(RTPClient client,DatagramPacketGroup group);
	
	public abstract void onInvite(RTPClient client,MapMessage message);
	
	public abstract void onInviteReplyed(RTPClient client,MapMessage message);
	
}
