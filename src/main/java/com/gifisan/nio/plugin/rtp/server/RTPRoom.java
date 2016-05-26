package com.gifisan.nio.plugin.rtp.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.IOSession;

public class RTPRoom {

	public static final int				MAX_ENDPOINT	= 1 << 6;
	private static AtomicInteger				autoRoomID 	= new AtomicInteger();
	
	private ReentrantLock					lock			= new ReentrantLock();
	private Map<InetSocketAddress, UDPEndPoint>	endPointMap	= new HashMap<InetSocketAddress, UDPEndPoint>();
	private List<UDPEndPoint>				endPoints		= new ArrayList<UDPEndPoint>();
	private Integer						roomID		= 0;
	private RTPRoomFactory					roomFactory	= null;
	private RTPContext						context		= null;
	private static final Logger				logger		= LoggerFactory.getLogger(RTPRoom.class);
	
	
	public Integer getRoomID() {
		return roomID;
	}

	public RTPRoom(RTPContext context,IOSession session) {
		this.roomID = genRoomID();
		this.roomFactory = context.getRTPRoomFactory();
		this.context = context;
		this.join(session.getUDPEndPoint());
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

	public void broadcast(UDPEndPoint endPoint, DatagramPacket packet) {
		
//		logger.debug("___________________broadcast,packet:{}",packet);

		ReentrantLock lock = this.lock;

		lock.lock();

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
			
//			logger.debug("___________________send to client,packet:{}",packet);
		}

		lock.unlock();
	}

	public boolean join(UDPEndPoint endPoint) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			if (endPoints.size() < MAX_ENDPOINT) {

				InetSocketAddress address = endPoint.getRemoteSocketAddress();

				if (!endPointMap.containsKey(address)) {
					
					endPointMap.put(address, endPoint);
					
					endPoints.add(endPoint);
					
					IOSession session = (IOSession) endPoint.getTCPSession();
					
					RTPSessionAttachment attachment = (RTPSessionAttachment) session.getAttachment(context);
					
					attachment.setRTPRoom(this);
					
					return true;
				}
				return false;
			}
			
			return false;
		} finally{
			lock.unlock();
		}
	}

	public void leave(UDPEndPoint endPoint) {

		ReentrantLock lock = this.lock;

		lock.lock();

		InetSocketAddress address = endPoint.getRemoteSocketAddress();

		if (endPointMap.containsKey(address)) {
			endPointMap.remove(address);
			endPoints.remove(endPoint);
			roomFactory.removeRTPRoom(roomID);
		}

		lock.unlock();
	}

	public static void main(String[] args) {
		System.out.println(1 << 16);
		System.out.println((2 << 7) - 1);
		System.out.println(2 << 15);
		System.out.println(255 * 255);
		System.out.println((2 << 30) - 1);
		System.out.println(Integer.MAX_VALUE);
	}

}
