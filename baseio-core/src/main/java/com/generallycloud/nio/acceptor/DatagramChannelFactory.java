package com.generallycloud.nio.acceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.NioDatagramChannel;
import com.generallycloud.nio.component.SelectorLoop;

public class DatagramChannelFactory {

	private Map<SocketAddress, DatagramChannel>	channels	= new HashMap<SocketAddress, DatagramChannel>();

	public DatagramChannel getDatagramChannel(SelectorLoop selectorLoop, java.nio.channels.DatagramChannel nioChannel, InetSocketAddress remote)
			throws SocketException {

		DatagramChannel channel = channels.get(remote);

		if (channel == null) {
			channel = new NioDatagramChannel(selectorLoop, nioChannel, remote);
			channels.put(remote, channel);
		}

		return channel;
	}

	public void removeDatagramChannel(DatagramChannel channel) {
		channels.remove(channel.getRemoteSocketAddress());
	}
}
