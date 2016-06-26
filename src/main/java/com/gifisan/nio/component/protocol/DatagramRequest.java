package com.gifisan.nio.component.protocol;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Parameters;

public class DatagramRequest {

	private String serviceName;
	
	private Parameters parameters;

	public DatagramRequest(String content) {
		this.parameters = new DefaultParameters(content);
		this.serviceName = parameters.getParameter("serviceName");
	}

	public String getServiceName() {
		return serviceName;
	}

	public Parameters getParameters() {
		return parameters;
	}
	
	public static DatagramRequest create(DatagramPacket packet,NIOContext context){
		if (packet.getTimestamp() == 0) {
			String param = new String(packet.getData(),context.getEncoding());
			
			return new DatagramRequest(param);
		}
		return null;
	}
	
}
