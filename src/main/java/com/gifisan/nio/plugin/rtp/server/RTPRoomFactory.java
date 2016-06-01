package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.ReentrantMap;

public class RTPRoomFactory {
	
	private ReentrantMap rooms = new ReentrantMap();

	public RTPRoom getRTPRoom(Integer roomID){
		return (RTPRoom) rooms.getValue(roomID);
	}
	
	public void removeRTPRoom(Integer roomID){
		rooms.remove(roomID);
	}
	
	public void putRTPRoom(RTPRoom room){
		
		rooms.add(room);
	}
}
