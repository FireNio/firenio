package com.generallycloud.nio.component;

public interface SocketChannelSelectionAlpha extends SelectionAcceptor {

	public ChannelFlusher getChannelFlusher();

	public void setChannelFlusher(ChannelFlusher channelFlusher);
}
