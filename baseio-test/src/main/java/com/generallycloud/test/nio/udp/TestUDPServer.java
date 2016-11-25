package com.generallycloud.test.nio.udp;

import java.io.IOException;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.acceptor.DatagramChannelAcceptor;
import com.generallycloud.nio.common.DebugUtil;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramChannelContextImpl;
import com.generallycloud.nio.component.DatagramPacketAcceptor;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.LoggerDatagramSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.DatagramPacket;

public class TestUDPServer {

	public static void main(String[] args) throws IOException {
		
		DatagramPacketAcceptor datagramPacketAcceptor = new DatagramPacketAcceptor() {
			
			public void accept(DatagramSession session, DatagramPacket packet) throws IOException {
				
				String req = packet.getDataString(Encoding.UTF8);
				
				DebugUtil.debug(req);
				
				DatagramPacket res = new DatagramPacket(("yes ," + req).getBytes(Encoding.UTF8));

				session.sendPacket(res);
			}
		};

		DatagramChannelContext context = new DatagramChannelContextImpl(new ServerConfiguration(18500));

		context.setDatagramPacketAcceptor(datagramPacketAcceptor);
		
		DatagramChannelAcceptor acceptor = new DatagramChannelAcceptor(context);

		context.addSessionEventListener(new LoggerDatagramSEListener());

		acceptor.bind();
	}

}
