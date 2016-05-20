package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class RTPClientDPAcceptor implements DatagramPacketAcceptor{
	
	private Logger logger = LoggerFactory.getLogger(RTPClientDPAcceptor.class);
	
	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {
		ClientSession session = (ClientSession) endPoint.getTCPSession();
		
		DatagramPacketAcceptor acceptor = session.getDatagramPacketAcceptor();
		
		if (acceptor == null) {
//			logger.debug("acceptor == null___________________data:{},_____packet:{}",new String(packet.getData()),packet.getSequenceNo());
			return;
		}
		
//		logger.debug("___________________data:{},_____seq:{}",new String(packet.getData()),packet.getSequenceNo());
		
		acceptor.accept(endPoint, packet);
		
		
	}
	
}
