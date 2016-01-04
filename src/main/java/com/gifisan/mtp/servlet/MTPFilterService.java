package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public interface MTPFilterService extends  LifeCycle{
	
	public boolean doFilter(Request request, Response response) throws Exception;
	
}
