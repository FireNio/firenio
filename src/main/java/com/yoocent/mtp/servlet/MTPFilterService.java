package com.yoocent.mtp.servlet;

import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.ServletAcceptAble;

public interface MTPFilterService extends ServletAcceptAble , LifeCycle , ChainFilter{

	public boolean doFilter(Request request, Response response) throws Exception;
	
}
