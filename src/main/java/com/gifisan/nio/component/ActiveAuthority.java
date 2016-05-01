package com.gifisan.nio.component;

public interface ActiveAuthority extends Authority{
	
	public abstract void author(String secretKey);
	
	public abstract void unauthor();
}
