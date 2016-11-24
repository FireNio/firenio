package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.DatagramReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class DatagramSessionImpl extends SessionImpl implements DatagramSession{
	
	protected DatagramChannel channel;

	public DatagramSessionImpl(DatagramChannel channel, Integer sessionID) {
		super(channel.getContext(), sessionID);
		this.channel = channel;
	}

	public void flush(ReadFuture future) {
		
		DatagramReadFuture f = (DatagramReadFuture) future;
		
		
	}

	protected Channel getChannel() {
		return channel;
	}

	
}
