package com.generallycloud.nio.component;

import com.generallycloud.nio.Looper;

public interface ChannelFlusher extends Looper{
	
	public abstract void fire(ChannelFlusherEvent event);

	public abstract void offer(SocketChannel channel);
	
	public abstract void wekeupSocketChannel(SocketChannel channel);
	
	public interface ChannelFlusherEvent{
		
		void handle(ChannelFlusher channelFlusher);
	}

}