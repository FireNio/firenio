package com.generallycloud.nio.extend.plugin.rtp.client;

import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.protocol.DatagramPacketGroup;

public abstract class RTPHandle {
	
	public abstract void onReceiveUDPPacket(RTPClient client,DatagramPacketGroup group);
	
	public abstract void onInvite(RTPClient client,MapMessage message);
	
	public abstract void onInviteReplyed(RTPClient client,MapMessage message);
	
	public abstract void onBreak(RTPClient client,MapMessage message);
	
}
