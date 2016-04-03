package com.gifisan.nio.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.component.AbstractEndPoint;
import com.gifisan.nio.component.EndPoint;

public class ClientEndPoint extends AbstractEndPoint implements EndPoint {

	public ClientEndPoint(SelectionKey selectionKey, ClientConnector clientConnector) throws SocketException {
		super(selectionKey);
		this.clientConnector = clientConnector;
	}

	private ClientConnector		clientConnector	= null;
	private EndPointInputStream	inputStream		= null;

	public EndPointInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(EndPointInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void register(Selector selector, int option) throws ClosedChannelException {
		channel.register(selector, option);
	}

	public int sessionSize() {
		return clientConnector.getClientSesssionSize();
	}

	public int write(ByteBuffer buffer) throws IOException {
		channel.write(buffer);

		for (; buffer.hasRemaining();) {

			channel.write(buffer);

		}
		return buffer.limit();
	}

	public int read(ByteBuffer buffer) throws IOException {
		channel.read(buffer);

		for (; buffer.hasRemaining();) {

			channel.read(buffer);

		}
		return buffer.limit();
	}

	public void write(byte[] beat) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(beat);
		buffer.flip();
		this.write(buffer);
	}
}
