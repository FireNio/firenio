package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ReadFuture;

public class RedisIOEventHandle extends IoEventHandleAdaptor{
	
	private Waiter<RedisNode> waiter;

	public void accept(Session session, ReadFuture future) throws Exception {
		
		RedisReadFuture f = (RedisReadFuture) future;
		
		Waiter<RedisNode> waiter = this.waiter;
		
		if (waiter != null) {
			
			this.waiter = null;
			
			waiter.setPayload(f.getRedisNode());
		}
		
	}

	public void setWaiter(Waiter<RedisNode> waiter) {
		this.waiter = waiter;
	}
	
}
