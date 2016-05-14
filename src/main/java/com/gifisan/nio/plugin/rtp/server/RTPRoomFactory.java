package com.gifisan.nio.plugin.rtp.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RTPRoomFactory {

	private Map<Integer, RTPRoom> rooms = new HashMap<Integer, RTPRoom>();
	
	private ReentrantLock lock = new ReentrantLock();
	
	
	public RTPRoom getRTPRoom(Integer roomID){
		return rooms.get(roomID);
	}
	
	public void removeRTPRoom(Integer roomID){
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		rooms.remove(roomID);
		
		lock.unlock();
	}
	
	public void putRTPRoom(RTPRoom room){
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		rooms.put(room.getRoomID(), room);
		
		lock.unlock();
	}
	
	
	
}
