package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketSessionFactoryImpl;
import com.generallycloud.nio.component.UnsafeSocketSession;

public class BalanceFacadeSocketSessionFactory extends SocketSessionFactoryImpl{

	public UnsafeSocketSession newUnsafeSession(SocketChannel channel) {
		return new BalanceFacadeSocketSessionImpl(channel, channel.getChannelID());
	}
	
}
