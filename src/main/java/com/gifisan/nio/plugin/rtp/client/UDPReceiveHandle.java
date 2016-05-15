package com.gifisan.nio.plugin.rtp.client;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.TextMessage;

public abstract class UDPReceiveHandle {

	public void onMessage(RTPClient client,Message message){

		if (message instanceof TextMessage) {

			TextMessage m = (TextMessage)message;
			
			Configuration configuration = new Configuration(m.getText());
			
			String cmd = configuration.getProperty("cmd");
			
			if ("invite".equals(cmd)) {
				onInvite(client,m, configuration);
			}else if("invite-reply".equals(cmd)){
				onInviteReplyed(client,m, configuration);
			}else{
				onOtherMessage(client,m,configuration);
			}
		}else{
			onOtherMessage(client,message);
		}
	}
	
	public void onOtherMessage(RTPClient client,Message message){
		
	}
	
	public void onOtherMessage(RTPClient client,TextMessage message,Configuration configuration){
		
	}
	
	public abstract void onReceiveUDPPacket(RTPClient client,DatagramPacket packet);
	
	public abstract void onInvite(RTPClient client,TextMessage message,Configuration configuration);
	
	public abstract void onInviteReplyed(RTPClient client,TextMessage message,Configuration configuration);
	
}
