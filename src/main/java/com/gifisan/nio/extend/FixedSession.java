package com.gifisan.nio.extend;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.ClientStreamAcceptor;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.security.Authority;

//FIXME   request // listen + write
public interface FixedSession extends ReadFutureAcceptor{

	public abstract ReadFuture request(String serviceName, String content,long timeout) throws IOException;

	public abstract ReadFuture request(String serviceName, String content, InputStream inputStream,long timeout) throws IOException;
	
	public abstract ReadFuture request(String serviceName, String content) throws IOException;

	public abstract ReadFuture request(String serviceName, String content, InputStream inputStream) throws IOException;
	
	public abstract void write(String serviceName, String content) throws IOException;

	public abstract void write(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void listen(String serviceName, OnReadFuture onReadFuture) throws IOException;

	public abstract void onStreamRead(String serviceName, ClientStreamAcceptor acceptor);
	
	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();
	
	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);
	
	public abstract void setAuthority(Authority authority);
	
	public abstract Session getSession();
	
	public abstract void update(Session session);
	
	public abstract RESMessage login4RES(String username, String password) ;
	
	public abstract boolean login(String username, String password);
	
	public abstract NIOContext getContext();
	
	public abstract void bindUDPSession() throws IOException;

	public abstract Authority getAuthority();
	
}