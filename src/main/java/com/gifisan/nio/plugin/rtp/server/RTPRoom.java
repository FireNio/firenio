package com.gifisan.nio.plugin.rtp.server;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContextUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.concurrent.ReentrantList;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.server.MQContext;
import com.gifisan.nio.plugin.jms.server.MQContextFactory;
import com.gifisan.security.Authority;

public class RTPRoom {

	private static AtomicInteger		autoRoomID	= new AtomicInteger();
	private static final Logger		logger		= LoggerFactory.getLogger(RTPRoom.class);
	public static final int		MAX_ENDPOINT	= 1 << 6;

	private RTPContext				context		= null;
	private ReentrantList<UDPEndPoint>	endPointList	= new ReentrantList<UDPEndPoint>();
	private RTPRoomFactory			roomFactory	= null;
	private Integer				roomID		= 0;
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

		for (;;) {
			int id = autoRoomID.get();

			int _next = id + 1;

			if (_next > MAX_ENDPOINT) {
				_next = 0;
			}

			if (autoRoomID.compareAndSet(id, _next))
				return _next;
		}
	}

	public Integer getRoomID() {
		return roomID;
	}

	public boolean join(UDPEndPoint endPoint) {

		ReentrantLock lock = endPointList.getReentrantLock();

		lock.lock();

		if (closed) {

			lock.unlock();

			return false;
		}

		if (endPointList.size() > MAX_ENDPOINT) {

			lock.unlock();

			return false;
		}

		if (!endPointList.add(endPoint)) {

			lock.unlock();

			return false;
		}

		lock.unlock();

		Session session = (Session) endPoint.getSession();

		RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context);

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

			MQContext mqContext = MQContextFactory.getMQContext();

			mqContext.offerMessage(message);
		}

		if (endPointList.size() == 0) {

			this.closed = true;

			roomFactory.removeRTPRoom(roomID);
		}

		lock.unlock();
	}

}
