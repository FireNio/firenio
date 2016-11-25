package com.generallycloud.nio.component;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;

public interface DatagramChannelContext extends ChannelContext {

	public abstract DatagramChannelFactory getDatagramChannelFactory();

	public abstract void setDatagramChannelFactory(DatagramChannelFactory datagramChannelFactory);

	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);

}