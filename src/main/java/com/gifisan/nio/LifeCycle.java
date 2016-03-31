package com.gifisan.nio;


public interface LifeCycle {
	
	public static int STARTING = 1;
	
	public static int RUNNING  = 2;
	
	public static int STOPPING = 3;
	
	public static int STOPPED  = 4;
	
	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

}
