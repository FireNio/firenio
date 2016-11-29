package com.generallycloud.nio.component;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public abstract class AbstractEventLoopThread implements EventLoopThread {

	private static final Logger logger = LoggerFactory.getLogger(AbstractEventLoopThread.class);
	
	private volatile boolean running = false;
	
	private boolean working = false;
	
	private boolean stoping = false;
	
	private byte[] workingLock = new byte[]{};
	
	private byte[] sleepingLock = new byte[]{};
	
	private Thread			monitor;
	
	public void loop() {
		
		for(;;){
			
			if (!running) {
				notify4Free();
				return;
			}
			
			working = true;
			
			if (stoping) {
				working = false;
				notify4Free();
				return;
			}
			
			try {
				
				doLoop();
				
			} catch (Throwable e) {
				logger.error(e.getMessage(),e);
			}
			
			working = false;
		}
	}
	
	protected abstract void doLoop();
	
	protected void beforeStop(){}

	public void stop() {
		
		synchronized (this) {
			
			if (!running) {
				return;
			}
			
			try {
				beforeStop();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
			running = false;
			
			stoping = true;
			
			if (working) {
				
				wait4Free();
				
				wakeupThread();
			}
			
			stoping = false;
			
			doStop();
		}
	}
	
	protected void doStop(){}
	
	private void wait4Free(){
		synchronized (workingLock) {
			try {
				workingLock.wait();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	private void notify4Free(){
		synchronized (workingLock) {
			workingLock.notify();
		}
	}

	protected void sleep(long time){
		synchronized (sleepingLock) {
			try {
				sleepingLock.wait(time);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	protected void wakeupThread(){
		synchronized (sleepingLock) {
			sleepingLock.notify();
		}
	}

	public void startup(String threadName) throws Exception {
		
		synchronized (this) {
		
			if (running) {
				return;
			}
			
			running = true;
		
			this.monitor = new Thread(new Runnable() {
	
				public void run() {
					loop();
				}
			}, threadName);
			
			this.doStartup();
			
			this.monitor.start();
		}
	}
	
	public boolean isMonitor(Thread thread) {
		return monitor == thread;
	}

	public Thread getMonitor() {
		return monitor;
	}
	
	protected void doStartup() throws Exception{
		
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isStopping() {
		return stoping;
	}
	
}
