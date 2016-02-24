package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.NIOSelectionReader;
import com.gifisan.nio.component.NIOSelectionWriter;
import com.gifisan.nio.concurrent.BlockingQueueThreadPool;
import com.gifisan.nio.server.selector.SelectionAccept;

public class NIOSelectionAcceptor extends AbstractLifeCycle implements SelectionAcceptor {

	private SelectionAccept[]		acceptors			= new SelectionAccept[5];
	private ServerContext			context			= null;
	private BlockingQueueThreadPool	servletThreadPool	= null;

	public NIOSelectionAcceptor(ServerContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		int opt = selectionKey.readyOps();

		acceptors[opt].accept(selectionKey);
	}

	protected void doStart() throws Exception {
		SharedBundle bundle = SharedBundle.instance();
		int CORE_SIZE = bundle.getIntegerProperty("SERVER.CORE_SIZE", 4);
		
		this.servletThreadPool = new BlockingQueueThreadPool("Servlet-accept-Job", CORE_SIZE);
		this.servletThreadPool.start();
		
		this.acceptors[1] = new NIOSelectionReader(context,servletThreadPool);
		this.acceptors[4] = new NIOSelectionWriter();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(servletThreadPool);
	}

}
