package com.generallycloud.nio.component;

public interface TCPSelectionAlpha extends SelectionAcceptor{

	public ChannelWriter getChannelWriter() ;

	public void setChannelWriter(ChannelWriter channelWriter) ;
}
