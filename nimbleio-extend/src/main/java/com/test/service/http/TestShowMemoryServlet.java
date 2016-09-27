package com.test.service.http;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.generallycloud.nio.buffer.MemoryPoolV3;
import com.generallycloud.nio.common.HtmlUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.protocol.http11.HttpContext;
import com.generallycloud.nio.component.protocol.http11.HttpSession;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;

public class TestShowMemoryServlet extends HTTPFutureAcceptorService{
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		NIOContext context = session.getIOSession().getContext();
		HttpContext httpContext = session.getContext();
		
		BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
		BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
		BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);
		
		MemoryPoolV3 heap = (MemoryPoolV3) context.getHeapByteBufferPool();
		
		int M = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();
		StringBuilder builder = new StringBuilder(HtmlUtil.HTML_HEADER);
		
		builder.append("		<div style=\"margin-left:20px;\">\n");
		builder.append("服务器内存使用情况：</BR>\n");
		builder.append("虚拟机占用内存：");
		builder.append(runtime.totalMemory()/M);
		builder.append("M;\n</BR>已占用内存：");
		builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
		builder.append("M;\n</BR>空闲内存：");
		builder.append(runtime.freeMemory() / M);
		builder.append("M;\n</BR>内存池状态（Heap）：");
		builder.append(heap.toSimpleString());
		builder.append(";\n</BR>服务器当前连接数（io-session）：");
		builder.append(context.getSessionFactory().getManagedSessionSize());
		builder.append(";\n</BR>服务器当前会话数（http-session）：");
		builder.append(httpContext.getHttpSessionFactory().getManagedSessionSize());
		builder.append(";\n</BR>服务运行时间：");
		builder.append(hour + "H;");
		builder.append("		</div>\n");
		builder.append("		<hr>\n");
		builder.append(HtmlUtil.HTML_BOTTOM);
		
		future.write(builder.toString());
		
		session.flush(future);
	}
	
}
