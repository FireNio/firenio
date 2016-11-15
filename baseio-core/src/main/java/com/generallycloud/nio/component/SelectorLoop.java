package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

import com.generallycloud.nio.Looper;
import com.generallycloud.nio.buffer.ByteBufAllocator;

public interface SelectorLoop extends SelectionAcceptor, Looper {

	public abstract Selector buildSelector(SelectableChannel channel) throws IOException;
	
	public abstract Selector getSelector();
	
	public abstract ChannelFlusher getChannelFlusher();
	
	public abstract BaseContext getContext();
	
	public abstract void startup() throws IOException;
	
	public abstract SelectableChannel getSelectableChannel();
	
	public abstract ByteBufAllocator getByteBufAllocator();
}
