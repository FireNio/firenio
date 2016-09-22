package com.generallycloud.nio.acceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.NioDatagramChannel;

public class DatagramChannelFactory {

	private Map<SocketAddress, DatagramChannel>	channels	= new HashMap<SocketAddress, DatagramChannel>();

	public DatagramChannel getDatagramChannel(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {

		DatagramChannel channel = channels.get(remote);

		if (channel == null) {
			channel = new NioDatagramChannel(context, selectionKey, remote);
			selectionKey.attach(channel);
			channels.put(remote, channel);
		}

		return channel;
	}

	public void removeDatagramChannel(DatagramChannel channel) {
		channels.remove(channel.getRemoteSocketAddress());
	}
}
