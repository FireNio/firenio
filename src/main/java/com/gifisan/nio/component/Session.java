package com.gifisan.nio.component;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.NIOContext;

public interface Session extends Attributes{

	public abstract void addEventListener(SessionEventListener listener);

	public abstract void setAttachment(Attachment attachment);

	public abstract Attachment getAttachment();

	public abstract void setAttachment(PluginContext context, Attachment attachment);

	public abstract Attachment getAttachment(PluginContext context);

	public abstract long getCreationTime();

	public abstract NIOContext getContext();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime() throws SocketException;

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();

	public abstract int getRemotePort();

	public abstract boolean isBlocking();

	public abstract boolean isOpened();

	public abstract String getSessionID();

	public abstract void destroy();

	public abstract boolean closed();
	
	public abstract String getMachineType() ;

}