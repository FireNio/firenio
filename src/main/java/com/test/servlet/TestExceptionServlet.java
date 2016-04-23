package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class TestExceptionServlet extends NIOServlet{

	public void accept(IOSession session,ReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}
