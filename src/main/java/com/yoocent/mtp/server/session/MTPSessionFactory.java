package com.yoocent.mtp.server.session;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.server.context.ServletContext;

public class MTPSessionFactory extends AbstractLifeCycle implements Job{

	private SchedulerFactory factory = new StdSchedulerFactory();
	
	private Scheduler scheduler = null;
	
	private HashMap<String, Session> sessions = new HashMap<String, Session>();

	public Session getSession(ServletContext context,String sessionID) {
		Session session = sessions.get(sessionID);
		if (session == null) {
			synchronized (sessions) {
				session = sessions.get(sessionID);
				if (session == null) {
					session = new MTPSession(context, sessionID);
				}
				sessions.put(sessionID, session);
			}
		}else{
			session.active();
		}
		return session;
	}
	
	protected void doStart() throws Exception {
		this.startManagerSession();
	}

	protected void doStop() throws Exception {
		this.cancelManagerSession();
	}


	public synchronized void cancelManagerSession() throws SchedulerException {
		scheduler.shutdown();
	}
	
	public synchronized void startManagerSession() throws SchedulerException {
		
		final String JOB = "SESSION";
		final String GROUP = "COM.BOKESOFT.THIRDPARTY.WEIXIN";
		final String TRIGGER = "trigger";
		
		scheduler = factory.getScheduler();
		// @NOTICE 任务的开始时间，nextGivenSecondDate方法表示：当前时间之后，
		// 每当秒数是13的倍数都是触发时间，当然只触发一次
		// 比如：00:00:12秒开始主线程，则13秒就会触发任务，
		// 如果00:00:14秒开始主线程，则在26秒触发任务
		
		//String ONE_MIN_CLEAR_ONCE = "0 0/1 * * * ?";
		String FIVE_MIN_CLEAR_ONCE = "0 0/5 * * * ?";
		Date runTime = DateBuilder.nextGivenSecondDate(new Date(), 1);
		JobDetail job = JobBuilder.newJob(MTPSessionFactory.class).withIdentity(JOB, GROUP)
				.build();
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER, GROUP)
//					0 0/30 * * * ?  --------  0/1 * * * * ?
				.withSchedule(CronScheduleBuilder.cronSchedule(FIVE_MIN_CLEAR_ONCE)).startAt(runTime)
				.build();
		scheduler.scheduleJob(job, trigger);
		scheduler.start();
	}
	
	public synchronized void stopManagerSession() throws SchedulerException {
		scheduler.clear();
	}

	private int lastSize = 0;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Map<String, Session> sessions = this.sessions;
		Collection<Session> sessionCollection = sessions.values();
		Session[] sessionArray = sessionCollection.toArray(new Session[0]);
		for (Session session : sessionArray) {
			if (!session.isValid()) {
				synchronized (sessions) {
					sessions.remove(session.getSessionID());
				}
			}
		}
		if (sessions.size() != lastSize) {
			System.out.println("[MTPServer] 回收过期会话，剩余数量："+sessions.size());
		}
	}
}
