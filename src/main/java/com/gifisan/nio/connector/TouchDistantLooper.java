package com.gifisan.nio.connector;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractLooper;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.implementation.SYSTEMBeatPacketServlet;

public class TouchDistantLooper extends AbstractLooper {
	
	private Logger logger = LoggerFactory.getLogger(TouchDistantLooper.class);

	private FixedSession session;
	
	private long time;

	public TouchDistantLooper(FixedSession session,long time) {
		this.session = session;
		this.time = time;
	}

	public void loop() {
		
		try {
			
			session.request(SYSTEMBeatPacketServlet.SERVICE_NAME, null);
			
			logger.debug("收到心跳回报!");
			
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		
		sleep(time);
	}
}
