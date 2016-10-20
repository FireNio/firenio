package com.generallycloud.nio.acceptor;

import java.io.IOException;

import com.generallycloud.nio.common.Unbindable;
import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.protocol.ReadFuture;

public interface ChannelAcceptor extends Unbindable, ChannelService {

	public abstract void bind() throws IOException;

	public abstract void broadcast(ReadFuture future);
	
	public abstract void offerSessionMEvent(SessionMEvent event);
	
	public abstract int getManagedSessionSize();
}
