package com.generallycloud.nio;

public interface Stopable {

	//FIXME stop 之前处理剩下的资源
	public abstract void stop();
	
}
