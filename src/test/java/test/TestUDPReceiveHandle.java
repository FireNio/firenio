package test;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.component.protocol.DatagramPacketFactory;
import com.gifisan.nio.component.protocol.DatagramPacketGroup;
import com.gifisan.nio.component.protocol.DatagramPacketGroup.DPForeach;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.rtp.RTPException;
import com.gifisan.nio.plugin.rtp.client.FixedClientDPAcceptor;
import com.gifisan.nio.plugin.rtp.client.RTPClient;
import com.gifisan.nio.plugin.rtp.client.UDPReceiveHandle;

public class TestUDPReceiveHandle extends UDPReceiveHandle {

	private String	customerID		= null;

	private String	otherCustomerID	= null;
	
	private static final Logger logger = LoggerFactory.getLogger(TestUDPReceiveHandle.class);
	
	private static int sleep = 100;
	
	protected TestUDPReceiveHandle(String customerID, String otherCustomerID) {
		this.customerID = customerID;
		this.otherCustomerID = otherCustomerID;
	}

	public void onReceiveUDPPacket(RTPClient client, DatagramPacketGroup group) {

		group.foreach(new DPForeach() {
			public void onPacket(DatagramPacket packet) {
				String data = new String(packet.getData(), Encoding.GBK);
				logger.debug("_______________foreach___data:{},seq:{}", data,packet.getSequenceNo());
			}
		});
	}

	public void onInvite(RTPClient client, MapMessage message, Parameters parameters) {

		int markInterval = 500;
		
		String roomID = parameters.getParameter("roomID");

		DatagramPacketFactory factory = new DatagramPacketFactory(markInterval);
		
		long currentMark = factory.getCalculagraph().getAlphaTimestamp();
		
		int groupSize = 102400;

		try {
			client.joinRoom(roomID);

			client.inviteReply(otherCustomerID,markInterval,currentMark,groupSize);
		} catch (RTPException e) {
			e.printStackTrace();
		}
		
		FixedClientDPAcceptor acceptor = new FixedClientDPAcceptor(markInterval,currentMark, groupSize, this, client);

		client.onDatagramPacketReceived(acceptor);
		
		client.setRoomID(roomID);

		

		for (int i = 0; i < 10000000; i++) {

			byte[] data = (customerID + i).getBytes();

			DatagramPacket packet = factory.createDatagramPacket(client.getRoomIDNo(), data);

			try {
				client.sendDatagramPacket(packet);
			} catch (RTPException e) {
				e.printStackTrace();
			}

			ThreadUtil.sleep(sleep);
		}
	}

	public void onInviteReplyed(RTPClient client, MapMessage message, Parameters parameters) {
		
		int markInterval = parameters.getIntegerParameter(RTPClient.MARK_INTERVAL);
		
		long currentMark = parameters.getLongParameter(RTPClient.CURRENT_MARK);

		int groupSize = parameters.getIntegerParameter(RTPClient.GROUP_SIZE);
		
		logger.debug("___________onInviteReplyed:{},{},{}",new Object[]{markInterval,currentMark,groupSize});
		
		DatagramPacketFactory factory = new DatagramPacketFactory(markInterval,currentMark);
		
		FixedClientDPAcceptor acceptor = new FixedClientDPAcceptor(markInterval,currentMark,groupSize, this, client);

		client.onDatagramPacketReceived(acceptor);

		for (int i = 0; i < 10000000; i++) {

			byte[] data = (customerID + i).getBytes();
			int roomID = client.getRoomIDNo();

			DatagramPacket packet = factory.createDatagramPacket(roomID, data);

			try {
				client.sendDatagramPacket(packet);
			} catch (RTPException e) {
				e.printStackTrace();
			}

			ThreadUtil.sleep(sleep);
		}
	}
}
