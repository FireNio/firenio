package com.yoocent.mtp.server;

import java.nio.channels.SelectionKey;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.component.BlockingQueueThreadPool;
import com.yoocent.mtp.component.NIOSelectionReader;
import com.yoocent.mtp.component.NIOSelectionWriter;
import com.yoocent.mtp.component.ServletService;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.server.selector.SelectionAcceptAble;

public class NIOConnectorHandle extends AbstractLifeCycle implements ConnectorHandle{

	private ServletService service = null;
	
	private SelectionAcceptAble [] acceptors = new SelectionAcceptAble[5];
	
	public void accept(SelectionKey selectionKey) throws Exception {
		int opt = selectionKey.readyOps();
		acceptors[opt].accept(selectionKey);
	}
	
	protected void doStart() throws Exception {
		ServletContext context = ServletContextFactory.getServletContext();
		
		this.service = new ServletService(context);
		this.service.start();
		this.threadPool.start();
		this.acceptors[1] =	new NIOSelectionReader(context,threadPool,service);
		this.acceptors[4] = new NIOSelectionWriter();
	}

	private BlockingQueueThreadPool threadPool = new BlockingQueueThreadPool();
	
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(service);
		LifeCycleUtil.stop(threadPool);
	}

}
