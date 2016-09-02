package com.generallycloud.nio.extend.plugin.rtp.server;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.UDPEndPoint;
import com.generallycloud.nio.component.concurrent.ReentrantList;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.Sequence;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.jms.server.MQContext;
import com.generallycloud.nio.extend.security.Authority;

//FIXME 是不是要限制最多room数
public class RTPRoom {

	private static final Logger		logger		= LoggerFactory.getLogger(RTPRoom.class);

	private RTPContext				context		;
	private ReentrantList<UDPEndPoint>	endPointList	= new ReentrantList<UDPEndPoint>();
	private RTPRoomFactory			roomFactory	;
	private Integer				roomID		;
	private boolean				closed		= false;

	public RTPRoom(RTPContext context, Session session) {
		this.roomID = genRoomID();
		this.roomFactory = context.getRTPRoomFactory();
		this.context = context;
		this.join(session.getUDPEndPoint());
	}

	public void broadcast(UDPEndPoint endPoint, DatagramPacket packet) {

		List<UDPEndPoint> endPoints = endPointList.getSnapshot();

		for (UDPEndPoint point : endPoints) {

			if (endPoint == point) {
				continue;
			}

			ByteBuffer buffer = packet.getSource();

			buffer.flip();

			try {
				point.sendPacket(buffer);
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

	public boolean join(UDPEndPoint endPoint) {
		
		if (endPoint == null) {
//			throw new RuntimeException("udpEndPoint is null");
			return false;
		}

		ReentrantLock lock = endPointList.getReentrantLock();

		lock.lock();

		if (closed) {

			lock.unlock();

			return false;
		}

		if (!endPointList.add(endPoint)) {

			lock.unlock();

			return false;
		}

		lock.unlock();

		Session session = (Session) endPoint.getSession();

		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context.getPluginIndex());

		attachment.setRTPRoom(this);

		return true;
	}

	public void leave(UDPEndPoint endPoint) {

		ReentrantLock lock = endPointList.getReentrantLock();

		lock.lock();

		endPointList.remove(endPoint);

		List<UDPEndPoint> endPoints = endPointList.getSnapshot();

		for (UDPEndPoint e : endPoints) {

			if (e == endPoint) {
				continue;
			}

			Session session = (Session) e.getSession();
			
			Authority authority = ApplicationContextUtil.getAuthority(session);

			MapMessage message = new MapMessage("mmm", authority.getUuid());

			message.setEventName("break");

			message.put("userID", authority.getUserID());

			MQContext mqContext = MQContext.getInstance();

			mqContext.offerMessage(message);
		}

		if (endPointList.size() == 0) {

			this.closed = true;

			roomFactory.removeRTPRoom(roomID);
		}

		lock.unlock();
	}

}
