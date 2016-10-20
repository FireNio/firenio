package com.generallycloud.nio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;

public abstract class AbstractLifeCycle implements LifeCycle {

	private boolean					failed				= false;
	private List<LifeCycleListener>		lifeCycleListeners		= new ArrayList<LifeCycleListener>();
	private Logger						logger				= LoggerFactory.getLogger(AbstractLifeCycle.class);
	private boolean					running				= false;
	private boolean					starting				= false;
	private boolean					stopped				= true;
	private boolean					stopping				= false;
	private Comparator<LifeCycleListener>	lifeCycleListenerSorter	= new Comparator<LifeCycleListener>() {

		public int compare(LifeCycleListener o1,
				LifeCycleListener o2) {
	
			return o1.lifeCycleListenerSortIndex() > o2
					.lifeCycleListenerSortIndex() ? 1
					: -1;
		}
	};

	public void addLifeCycleListener(LifeCycleListener listener) {
		synchronized (lifeCycleListeners) {
			lifeCycleListeners.add(listener);
			Collections.sort(lifeCycleListeners, lifeCycleListenerSorter);
		}
	}

	protected abstract void doStart() throws Exception;

	protected abstract void doStop() throws Exception;

	private void fireEvent(int event) {
		if (lifeCycleListeners.size() == 0) {
			return;
		}
		switch (event) {
		case STARTING:
			synchronized (lifeCycleListeners) {
				for (LifeCycleListener listener : lifeCycleListeners) {
					try {
						listener.lifeCycleStarting(this);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			break;
		case RUNNING:
			synchronized (lifeCycleListeners) {
				for (LifeCycleListener listener : lifeCycleListeners) {
					try {
						listener.lifeCycleStarted(this);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			break;
		case STOPPING:
			synchronized (lifeCycleListeners) {
				for (LifeCycleListener listener : lifeCycleListeners) {
					try {
						listener.lifeCycleStopping(this);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			break;
		case STOPPED:
			synchronized (lifeCycleListeners) {
				for (LifeCycleListener listener : lifeCycleListeners) {
					try {
						listener.lifeCycleStopped(this);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void fireFailed(Exception exception) {
		if (lifeCycleListeners.size() == 0) {
			return;
		}
		synchronized (lifeCycleListeners) {
			for (LifeCycleListener listener : lifeCycleListeners) {
				try {
					listener.lifeCycleFailure(this, exception);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public boolean isFailed() {
		return this.failed;
	}

	public boolean isRunning() {
		return this.running;
	}

	public boolean isStarted() {
		return this.running;
	}

	public boolean isStarting() {
		return this.starting;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public boolean isStopping() {
		return this.stopping;
	}

	public void removeLifeCycleListener(LifeCycleListener listener) {
		synchronized (lifeCycleListeners) {
			lifeCycleListeners.remove(listener);
			Collections.sort(lifeCycleListeners, lifeCycleListenerSorter);
		}
	}

	public void start() throws Exception {
		
		if (this.stopped != true && this.stopping != true) {
			throw new IllegalStateException("did not stopped");
		}

		this.starting = true;

		this.fireEvent(STARTING);

		try {

			this.doStart();

			LoggerUtil.prettyNIOServerLog(logger, "加载完成 [ {} ]", this.toString());

		} catch (Exception e) {

			this.failed = true;
			
			this.starting = false;

			this.fireFailed(e);

			throw e;
		}

		this.starting = false;

		this.running = true;
		
		this.stopped = false;
		
		this.stopping = false;

		this.fireEvent(RUNNING);

	}

	public void stop(){
		
//		if (this.starting != true && this.running != true) {
//			throw new IllegalStateException("stopped,"+this.toString());
//		}

		this.running = false;

		this.stopping = true;

		this.fireEvent(STOPPING);

		try {

			this.doStop();

			LoggerUtil.prettyNIOServerLog(logger, "卸载完成 [ {} ]", this.toString());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}

		this.stopping = false;

		this.stopped = true;
		
		this.fireEvent(STOPPED);

	}
	
	public void softStart(){
		
		this.starting = false;

		this.running = true;
		
		this.stopped = false;
		
		this.stopping = false;

	}

}
