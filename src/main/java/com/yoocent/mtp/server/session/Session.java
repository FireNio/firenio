package com.yoocent.mtp.server.session;

import com.yoocent.mtp.server.Attributes;
import com.yoocent.mtp.server.context.ServletContext;

public interface Session extends Attributes {

	public abstract void active();

	public abstract ServletContext getServletContext();

	public abstract long getCreationTime();

	public abstract String getSessionID();

	public abstract long getLastAccessedTime();

	public abstract long getMaxInactiveInterval();

	public abstract boolean isValid();

	public abstract void setMaxInactiveInterval(long millisecond);

}
