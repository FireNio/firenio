package com.generallycloud.nio.component;

public interface SocketChannelSelectionAlpha extends SelectionAcceptor{

	public ChannelWriter getChannelWriter() ;

	public void setChannelWriter(ChannelWriter channelWriter) ;
}
