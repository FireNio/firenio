package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.JsonParameters;
import com.generallycloud.nio.component.Parameters;

public class DatagramRequest {

	private String serviceName;
	
	private Parameters parameters;

	public DatagramRequest(String content) {
		this.parameters = new JsonParameters(content);
		this.serviceName = parameters.getParameter("serviceName");
	}

	public String getFutureName() {
		return serviceName;
	}

	public Parameters getParameters() {
		return parameters;
	}
	
	public static DatagramRequest create(DatagramPacket packet,DatagramChannelContext context){
		if (packet.getTimestamp() == 0) {
			String param = new String(packet.getData(),context.getEncoding());
			
			return new DatagramRequest(param);
		}
		return null;
	}
	
}
