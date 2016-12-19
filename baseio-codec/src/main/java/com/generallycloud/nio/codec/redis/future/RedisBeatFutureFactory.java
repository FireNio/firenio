package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.codec.redis.future.RedisReadFuture.RedisCommand;
import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class RedisBeatFutureFactory implements BeatFutureFactory{

	@Override
	public ReadFuture createPINGPacket(SocketSession session) {
		
		RedisCmdFuture f = new RedisCmdFuture(session.getContext());
		
		f.setPING();
		
		f.writeCommand(RedisCommand.PING.raw);
		
		return f;
	}

	@Override
	public ReadFuture createPONGPacket(SocketSession session) {
		
		RedisCmdFuture f = new RedisCmdFuture(session.getContext());
		
		f.setPONG();
		
		f.writeCommand(RedisCommand.PONG.raw);
		
		return f;
	}

	
}
