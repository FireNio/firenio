package com.gifisan.mtp.server.session;

import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerEndPoint;

public interface InnerSession extends Session {

	public abstract void destroyImmediately();
	
	public abstract Request updateRequest(ServerEndPoint endPoint);
	
	public abstract Response updateResponse(ServerEndPoint endPoint);

	public abstract ServletAcceptJob updateServletAcceptJob(ServerEndPoint endPoint);
	
}
