package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.component.concurrent.ReentrantMap;

public class RTPRoomFactory {

	private ReentrantMap<Integer, RTPRoom>	rooms	= new ReentrantMap<Integer, RTPRoom>();

	public RTPRoom getRTPRoom(Integer roomID) {
		return rooms.get(roomID);
	}

	public void removeRTPRoom(Integer roomID) {
		rooms.remove(roomID);
	}

	public void putRTPRoom(RTPRoom room) {

		rooms.put(room.getRoomID(), room);
	}
}
