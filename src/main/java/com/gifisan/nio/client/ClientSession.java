package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

//FIXME   request // listen + write
public interface ClientSession extends Session {

	public abstract ReadFuture request(String serviceName, String content) throws IOException;

	public abstract ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void write(String serviceName, String content) throws IOException;

	public abstract void write(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void listen(String serviceName, OnReadFuture onReadFuture) throws IOException;

	public abstract void cancelListen(String serviceName);

	public abstract ReadFuture poll(long timeout) throws DisconnectException;

	public abstract long getTimeout();

	public abstract void setTimeout(long timeout);

	public abstract ClientContext getContext();

	public abstract void onStreamRead(String key, ClientStreamAcceptor acceptor);
	
	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();

}