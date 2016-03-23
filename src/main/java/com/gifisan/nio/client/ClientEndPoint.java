package com.gifisan.nio.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.gifisan.nio.component.AbstractEndPoint;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.NIOException;

public class ClientEndPoint extends AbstractEndPoint implements EndPoint {


	public ClientEndPoint(SelectionKey selectionKey,ClientConnector	clientConnector)throws SocketException {
		super(selectionKey);
		this.clientConnector = null;
		this.channel = (SocketChannel) selectionKey.channel();
		this.clientConnector = clientConnector;
	}

	private SocketChannel	channel			= null;

	private ClientConnector	clientConnector	= null;


	public void close() throws IOException {
		channel.close();
	}

	public void register(Selector selector, int option) throws ClosedChannelException {
		channel.register(selector, option);
	}

	public int sessionSize() {
		return clientConnector.getClientSesssionSize();
	}

	public NIOException handleException(IOException exception) throws NIOException {
		return new NIOException(exception.getMessage(),exception);
	}

}
