package com.generallycloud.nio;

public interface Looper{
	
	public abstract void startup() throws Exception;

	public abstract void loop();
	
	//FIXME stop 之前处理剩下的资源
	public abstract void stop();
}
