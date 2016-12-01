package com.generallycloud.nio.container.rtp.client;

import com.generallycloud.nio.container.jms.MapMessage;
import com.generallycloud.nio.protocol.DatagramPacketGroup;

public abstract class RTPHandle {
	
	public abstract void onReceiveUDPPacket(RTPClient client,DatagramPacketGroup group);
	
	public abstract void onInvite(RTPClient client,MapMessage message);
	
	public abstract void onInviteReplyed(RTPClient client,MapMessage message);
	
	public abstract void onBreak(RTPClient client,MapMessage message);
	
}
