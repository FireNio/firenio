package com.generallycloud.nio.rtp;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.DatagramPacket;
import com.generallycloud.nio.protocol.DatagramRequest;

public abstract class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {
	
	private Logger logger = LoggerFactory.getLogger(ServerDatagramPacketAcceptor.class);

	public void accept(DatagramSession session, DatagramPacket packet) throws IOException {

		DatagramChannelContext context = session.getContext();

		DatagramRequest request = DatagramRequest.create(packet, context);

		if (request != null) {
			execute(session,request);
			return;
		}
		
//		logger.debug("___________________server receive,packet:{}",packet);
		
//		SocketSession session = channel.getSession();
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
//		doAccept(channel, packet,session); //FIXME UDP
	}
	
	protected abstract void doAccept(DatagramSession channel, DatagramPacket packet,SocketSession session) throws IOException ;
	
	protected abstract void execute(DatagramSession channel,DatagramRequest request) ;

}
