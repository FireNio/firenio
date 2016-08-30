package com.gifisan.nio.extend;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.security.Authority;

public interface FixedSession extends ReadFutureAcceptor, Closeable {

	public abstract void logout();

	public abstract NIOReadFuture request(String serviceName, String content) throws IOException;

	public abstract NIOReadFuture request(String serviceName, String content, InputStream inputStream)
			throws IOException;

	public abstract void write(String serviceName, String content) throws IOException;

	public abstract void write(String serviceName, String content, InputStream inputStream) throws IOException;

	public abstract void listen(String serviceName, OnReadFuture onReadFuture) throws IOException;

	public abstract void setAuthority(Authority authority);

	public abstract Session getSession();

	public abstract void update(Session session);

	public abstract RESMessage login4RES(String username, String password);

	public abstract boolean login(String username, String password);

	public abstract NIOContext getContext();

	public abstract Authority getAuthority();

	public abstract boolean isLogined();

	public abstract long getTimeout();

	public abstract void setTimeout(long timeout);
}