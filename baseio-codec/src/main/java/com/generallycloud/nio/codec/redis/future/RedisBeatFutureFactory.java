package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.codec.redis.future.RedisReadFuture.RedisCommand;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.ReadFuture;

public class RedisBeatFutureFactory implements BeatFutureFactory{

	public ReadFuture createPINGPacket(Session session) {
		
		RedisCmdFuture f = new RedisCmdFuture(session.getContext());
		
		f.setPING();
		
		f.writeCommand(RedisCommand.PING.raw);
		
		return f;
	}

	public ReadFuture createPONGPacket(Session session) {
		
		RedisCmdFuture f = new RedisCmdFuture(session.getContext());
		
		f.setPONG();
		
		f.writeCommand(RedisCommand.PONG.raw);
		
		return f;
	}

	
}
