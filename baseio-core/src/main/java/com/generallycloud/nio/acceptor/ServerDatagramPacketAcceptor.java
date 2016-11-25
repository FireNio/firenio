package com.generallycloud.nio.acceptor;

import java.io.IOException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.DatagramPacket;
import com.generallycloud.nio.protocol.DatagramRequest;

public abstract class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {
	
	private Logger logger = LoggerFactory.getLogger(ServerDatagramPacketAcceptor.class);

	public void accept(DatagramChannel channel, DatagramPacket packet) throws IOException {

		DatagramChannelContext context = channel.getContext();

		DatagramRequest request = DatagramRequest.create(packet, context);

		if (request != null) {
			execute(channel,request);
			return;
		}
		
//		logger.debug("___________________server receive,packet:{}",packet);
		
//		SocketSession session = channel.getSession();
		
		DatagramSession session = channel.getSession(); //FIXME UDP
		
		if (session == null) {
			logger.debug("___________________null session,packet:{}",packet);
			return;
		}
		
//		doAccept(channel, packet,session); //FIXME UDP
	}
	
	protected abstract void doAccept(DatagramChannel channel, DatagramPacket packet,SocketSession session) throws IOException ;
	
	protected abstract void execute(DatagramChannel channel,DatagramRequest request) ;

}
