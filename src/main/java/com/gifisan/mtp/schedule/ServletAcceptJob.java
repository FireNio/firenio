package com.gifisan.mtp.schedule;

import java.io.IOException;

import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletAcceptor;

public interface ServletAcceptJob extends ServletAcceptor, Job{

	public abstract void acceptException(IOException exception);
	
	public abstract ServletAcceptJob update(ServerEndPoint endPoint, Request request, Response response);
}
