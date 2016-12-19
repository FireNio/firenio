package com.generallycloud.nio.component;

public abstract class AbstractSessionManager implements SessionManager{

	private long						current_idle_time	= 0;
	private long						last_idle_time		= 0;
	private long						session_idle_time	= 0;
	private long						next_idle_time		= System.currentTimeMillis();

	public AbstractSessionManager(long session_idle_time) {
		this.session_idle_time = session_idle_time;
	}

	protected abstract void fireSessionManagerEvent();

	@Override
	public void loop() {

		fireSessionManagerEvent();

		long current_time = System.currentTimeMillis();

		if (next_idle_time > current_time) {
			return;
		}

		this.last_idle_time = this.current_idle_time;

		this.current_idle_time = current_time;

		this.next_idle_time = current_idle_time + session_idle_time;

		sessionIdle(last_idle_time, current_time);
	}
	
	protected abstract void sessionIdle(long lastIdleTime, long currentTime) ;

	
}
