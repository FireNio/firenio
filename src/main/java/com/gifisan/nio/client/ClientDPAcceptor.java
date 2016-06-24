package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class ClientDPAcceptor implements DatagramPacketAcceptor{
	
	private Logger logger = LoggerFactory.getLogger(ClientDPAcceptor.class);
	
	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {
		ConnectorSession session = (ConnectorSession) endPoint.getSession();
		
		DatagramPacketAcceptor acceptor = session.getDatagramPacketAcceptor();
		
		if (acceptor == null) {
			logger.debug("___________________null acceptor,packet:{}",packet);
			return;
		}
		
//		logger.debug("___________________client receive,packet:{}",packet);
		
		acceptor.accept(endPoint, packet);
		
		
	}
	
}
