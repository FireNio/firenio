package com.generallycloud.test.nio.udp;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.component.protocol.DatagramPacketFactory;
import com.generallycloud.nio.component.protocol.DatagramPacketGroup;
import com.generallycloud.nio.component.protocol.DatagramPacketGroup.DPForeach;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.rtp.RTPException;
import com.generallycloud.nio.extend.plugin.rtp.client.RTPClient;
import com.generallycloud.nio.extend.plugin.rtp.client.RTPClientDPAcceptor;
import com.generallycloud.nio.extend.plugin.rtp.client.RTPHandle;

public class TestUDPReceiveHandle extends RTPHandle {

	private Logger	logger			= LoggerFactory.getLogger(TestUDPReceiveHandle.class);
	private int	sleep			= 1;

	public void onReceiveUDPPacket(RTPClient client, DatagramPacketGroup group) {

		
//		logger.debug("_______________foreach___data_size:{}", group.size());
		group.foreach(new DPForeach() {
			public void onPacket(DatagramPacket packet) {
				String data = new String(packet.getData(), Encoding.GBK);
				logger.debug("_______________foreach___data:{},seq:{}", data, packet.getSequenceNo());
			}
		});
	}

	public void onInvite(RTPClient client, MapMessage message) {

		int markInterval = 5;

		String roomID = message.getParameter("roomID");
		
		String inviteUsername = message.getParameter("inviteUsername");
		
		DatagramPacketFactory factory = new DatagramPacketFactory(markInterval);

		long currentMark = factory.getCalculagraph().getAlphaTimestamp();

		int groupSize = 102400;

		try {
			client.joinRoom(roomID);

			client.inviteReply(inviteUsername, markInterval, currentMark, groupSize);
		} catch (RTPException e) {
			e.printStackTrace();
		}

		RTPClientDPAcceptor acceptor = new RTPClientDPAcceptor(markInterval, currentMark, groupSize, this, client);

		client.setRTPClientDPAcceptor(acceptor);

		client.setRoomID(roomID);

		for (int i = 0; i < 10000000; i++) {

			byte[] data = (inviteUsername+ i).getBytes();

			DatagramPacket packet = factory.createDatagramPacket(data);

			try {
				client.sendDatagramPacket(packet);
			} catch (RTPException e) {
				e.printStackTrace();
			}
			
//			logger.debug("________________________send_packet:{}",packet);

			ThreadUtil.sleep(sleep);
		}
	}

	public void onInviteReplyed(RTPClient client, MapMessage message) {

		int markInterval = message.getIntegerParameter(RTPClient.MARK_INTERVAL);

		long currentMark = message.getLongParameter(RTPClient.CURRENT_MARK);

		int groupSize = message.getIntegerParameter(RTPClient.GROUP_SIZE);

		logger.debug("___________onInviteReplyed:{},{},{}", new Object[] { markInterval, currentMark, groupSize });

		DatagramPacketFactory factory = new DatagramPacketFactory(markInterval, currentMark);

		RTPClientDPAcceptor acceptor = new RTPClientDPAcceptor(markInterval, currentMark, groupSize, this, client);

		client.setRTPClientDPAcceptor(acceptor);

		for (int i = 0; i < 10000000; i++) {

			byte[] data = (client.getInviteUsername() + i).getBytes();

			DatagramPacket packet = factory.createDatagramPacket(data);

			try {
				client.sendDatagramPacket(packet);
			} catch (RTPException e) {
				e.printStackTrace();
			}

//			logger.debug("________________________send_packet:{}",packet);
			
			ThreadUtil.sleep(sleep);
		}
	}

	public void onBreak(RTPClient client, MapMessage message) {
		
		logger.debug("_________________________leave,{}",message.toString());
	}
	
	
}
