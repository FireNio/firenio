package com.gifisan.nio.plugin.rtp.client;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.protocol.DatagramPacketGroup;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.Message;

public abstract class UDPReceiveHandle {

	public void onMessage(RTPClient client,Message message){

		if (message instanceof MapMessage) {

			MapMessage m = (MapMessage)message;
			
			Parameters parameters = new DefaultParameters(m.getJSONObject());
			
			String cmd = parameters.getParameter("cmd");
			
			if ("invite".equals(cmd)) {
				onInvite(client,m, parameters);
			}else if("invite-reply".equals(cmd)){
				onInviteReplyed(client,m, parameters);
			}else{
				onOtherMessage(client,m,parameters);
			}
		}else{
			onOtherMessage(client,message);
		}
	}
	
	public void onOtherMessage(RTPClient client,Message message){
		
	}
	
	public void onOtherMessage(RTPClient client,MapMessage message,Parameters parameters){
		
	}
	
	public abstract void onReceiveUDPPacket(RTPClient client,DatagramPacketGroup group);
	
	public abstract void onInvite(RTPClient client,MapMessage message,Parameters parameters);
	
	public abstract void onInviteReplyed(RTPClient client,MapMessage message,Parameters parameters);
	
}
