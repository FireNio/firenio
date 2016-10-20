package com.generallycloud.nio.extend;

import java.io.IOException;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.ReadFutureAcceptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.security.Authority;

public interface FixedSession extends ReadFutureAcceptor {

	public abstract void logout();

	public abstract BaseReadFuture request(String serviceName, String content) throws IOException;

	public abstract BaseReadFuture request(String serviceName, String content, byte[] binary) throws IOException;

	public abstract void write(String serviceName, String content) throws IOException;

	public abstract void write(String serviceName, String content, byte[] binary) throws IOException;

	public abstract void listen(String serviceName, OnReadFuture onReadFuture) throws IOException;

	public abstract void setAuthority(Authority authority);

	public abstract Session getSession();

	public abstract void update(Session session);

	public abstract RESMessage login4RES(String username, String password);

	public abstract boolean login(String username, String password);

	public abstract BaseContext getContext();

	public abstract Authority getAuthority();

	public abstract boolean isLogined();

	public abstract long getTimeout();

	public abstract void setTimeout(long timeout);
}