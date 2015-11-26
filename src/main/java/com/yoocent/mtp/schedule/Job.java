package com.yoocent.mtp.schedule;


public interface Job extends Runnable {

	public abstract void doJob() throws Exception;
	
}
