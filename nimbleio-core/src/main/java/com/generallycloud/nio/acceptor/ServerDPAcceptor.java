package com.generallycloud.nio.acceptor;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.component.protocol.DatagramRequest;

public abstract class ServerDPAcceptor implements DatagramPacketAcceptor {
	
	private Logger logger = LoggerFactory.getLogger(ServerDPAcceptor.class);

	public void accept(DatagramChannel channel, DatagramPacket packet) throws IOException {

		NIOContext nioContext = channel.getContext();

		DatagramRequest request = DatagramRequest.create(packet, nioContext);

		if (request != null) {
			execute(channel,request);
			return;
		}
		
//		logger.debug("___________________server receive,packet:{}",packet);
		
		Session session = channel.getSession();
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
		doAccept(channel, packet,session);
	}
	
	protected abstract void doAccept(DatagramChannel channel, DatagramPacket packet,Session session) throws IOException ;
	
	protected abstract void execute(DatagramChannel channel,DatagramRequest request) ;

}
