package com.generallycloud.nio.container.rtp.server;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.ReentrantList;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.Sequence;
import com.generallycloud.nio.container.rtp.RTPContext;
import com.generallycloud.nio.protocol.DatagramPacket;

//FIXME 是不是要限制最多room数
public class RTPRoom {

	private static final Logger		logger		= LoggerFactory.getLogger(RTPRoom.class);

	private RTPContext				context		;
	private ReentrantList<DatagramSession>	datagramChannelList	= new ReentrantList<DatagramSession>();
	private RTPRoomFactory			roomFactory	;
	private Integer				roomID		;
	private boolean				closed		= false;

	public RTPRoom(RTPContext context, Session session) {
		this.roomID = genRoomID();
		this.roomFactory = context.getRTPRoomFactory();
		this.context = context;
//		this.join(session.getDatagramChannel()); //FIXME udp 
	}

	public void broadcast(DatagramSession session, DatagramPacket packet) {

		List<DatagramSession> datagramChannels = datagramChannelList.getSnapshot();

		for (DatagramSession ch : datagramChannels) {

			if (session == ch) {
				continue;
			}

			try {
				ch.sendPacket(packet);
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

	public boolean join(DatagramSession session) {
		
		if (session == null) {
			return false;
		}

		ReentrantLock lock = datagramChannelList.getReentrantLock();

		lock.lock();

		if (closed) {

			lock.unlock();

			return false;
		}

		if (!datagramChannelList.add(session)) {

			lock.unlock();

			return false;
		}

		lock.unlock();

//		Session session = (Session) session.getSession();

		//FIXME RTP
//		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context.getPluginIndex());

//		attachment.setRTPRoom(this);

		return true;
	}

	public void leave(DatagramSession channel) {

		ReentrantLock lock = datagramChannelList.getReentrantLock();

		lock.lock();

		datagramChannelList.remove(channel);

		List<DatagramSession> chs = datagramChannelList.getSnapshot();

		for (DatagramSession ch : chs) {

			if (ch == channel) {
				continue;
			}

			//FIXME RTP
//			SocketSession session = (SocketSession) ch.getSession();
			
//			Authority authority = ApplicationContextUtil.getAuthority(session);
//
//			MapMessage message = new MapMessage("mmm", authority.getUuid());
//
//			message.setEventName("break");
//
//			message.put("userID", authority.getUserID());
//
//			MQContext mqContext = MQContext.getInstance();
//
//			mqContext.offerMessage(message);
		}

		if (datagramChannelList.size() == 0) {

			this.closed = true;

			roomFactory.removeRTPRoom(roomID);
		}

		lock.unlock();
	}

}
