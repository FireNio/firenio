package com.generallycloud.nio.extend.plugin.rtp.server;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.concurrent.ReentrantList;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.Sequence;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.jms.server.MQContext;
import com.generallycloud.nio.extend.security.Authority;
import com.generallycloud.nio.protocol.DatagramPacket;

//FIXME 是不是要限制最多room数
public class RTPRoom {

	private static final Logger		logger		= LoggerFactory.getLogger(RTPRoom.class);

	private RTPContext				context		;
	private ReentrantList<DatagramChannel>	datagramChannelList	= new ReentrantList<DatagramChannel>();
	private RTPRoomFactory			roomFactory	;
	private Integer				roomID		;
	private boolean				closed		= false;

	public RTPRoom(RTPContext context, Session session) {
		this.roomID = genRoomID();
		this.roomFactory = context.getRTPRoomFactory();
		this.context = context;
		this.join(session.getDatagramChannel());
	}

	public void broadcast(DatagramChannel channel, DatagramPacket packet) {

		List<DatagramChannel> datagramChannels = datagramChannelList.getSnapshot();

		for (DatagramChannel ch : datagramChannels) {

			if (channel == ch) {
				continue;
			}

			ByteBuffer buffer = packet.getSource();

			buffer.flip();

			try {
				ch.sendPacket(buffer);
			} catch (Throwable e) {
				logger.debug(e);
			}
		}
	}

	private Integer genRoomID() {

		Sequence sequence = ApplicationContext.getInstance().getSequence();
		
		return sequence.AUTO_ROOM_ID.getAndIncrement();
	}

	public Integer getRoomID() {
		return roomID;
	}

	public boolean join(DatagramChannel channel) {
		
		if (channel == null) {
			return false;
		}

		ReentrantLock lock = datagramChannelList.getReentrantLock();

		lock.lock();

		if (closed) {

			lock.unlock();

			return false;
		}

		if (!datagramChannelList.add(channel)) {

			lock.unlock();

			return false;
		}

		lock.unlock();

		Session session = (Session) channel.getSession();

		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context.getPluginIndex());

		attachment.setRTPRoom(this);

		return true;
	}

	public void leave(DatagramChannel channel) {

		ReentrantLock lock = datagramChannelList.getReentrantLock();

		lock.lock();

		datagramChannelList.remove(channel);

		List<DatagramChannel> chs = datagramChannelList.getSnapshot();

		for (DatagramChannel ch : chs) {

			if (ch == channel) {
				continue;
			}

			Session session = (Session) ch.getSession();
			
			Authority authority = ApplicationContextUtil.getAuthority(session);

			MapMessage message = new MapMessage("mmm", authority.getUuid());

			message.setEventName("break");

			message.put("userID", authority.getUserID());

			MQContext mqContext = MQContext.getInstance();

			mqContext.offerMessage(message);
		}

		if (datagramChannelList.size() == 0) {

			this.closed = true;

			roomFactory.removeRTPRoom(roomID);
		}

		lock.unlock();
	}

}
