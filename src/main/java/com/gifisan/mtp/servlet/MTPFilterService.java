package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletAccept;

public interface MTPFilterService extends ServletAccept , LifeCycle , ChainFilter{

	public boolean doFilter(Request request, Response response) throws Exception;
	
}
