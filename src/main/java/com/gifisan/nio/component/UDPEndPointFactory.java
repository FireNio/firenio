package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.server.NIOContext;

public class UDPEndPointFactory {

	private Map<Long, UDPEndPoint>	endPoints	= new HashMap<Long, UDPEndPoint>();
	
	public UDPEndPoint getUDPEndPoint(NIOContext context,SelectionKey selectionKey) throws SocketException{
		
		UDPEndPoint endPoint = (UDPEndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			endPoint = new DefaultUDPEndPoint(context, (DatagramChannel) selectionKey.channel());
			selectionKey.attach(endPoint);
			endPoints.put(endPoint.getEndPointID(), endPoint);
		}
		
		return endPoint;
	}
	
	public UDPEndPoint getUDPEndPoint(Long endPointID){
		return endPoints.get(endPointID);
	}
	
	public void removeUDPEndPoint(UDPEndPoint endPoint){
		endPoints.remove(endPoint.getEndPointID());
	}
}
