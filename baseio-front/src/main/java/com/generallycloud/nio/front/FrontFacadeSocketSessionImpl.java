package com.generallycloud.nio.front;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class FrontFacadeSocketSessionImpl extends UnsafeSocketSessionImpl implements FrontFacadeSocketSession {

	public FrontFacadeSocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	private long next_check_time;
	
	private int msg_size;
	
	@Override
	public boolean overfulfil(int size){
		
		long now = System.currentTimeMillis();
		
		if (now > next_check_time) {
			next_check_time = now + 1000;
			msg_size = 0;
		}
		
		return ++msg_size > size; 
	}
	
}
