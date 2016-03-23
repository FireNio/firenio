package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.NIOSelectionReader;
import com.gifisan.nio.component.NIOSelectionWriter;
import com.gifisan.nio.concurrent.BlockingQueueThreadPool;
import com.gifisan.nio.server.selector.SelectionAccept;

public class NIOSelectionAcceptor extends AbstractLifeCycle implements SelectionAcceptor {

	private SelectionAccept[]		acceptors			= new SelectionAccept[5];
	private ServerContext			context			= null;
	private BlockingQueueThreadPool	acceptorDispatch	= null;

	public NIOSelectionAcceptor(ServerContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		int opt = selectionKey.readyOps();

		acceptors[opt].accept(selectionKey);
	}

	protected void doStart() throws Exception {
		int CORE_SIZE = context.getServerCoreSize();
		
		this.acceptorDispatch = new BlockingQueueThreadPool("Service-acceptor", CORE_SIZE);
		this.acceptorDispatch.start();
		
		this.acceptors[1] = new NIOSelectionReader(context,acceptorDispatch);
		this.acceptors[4] = new NIOSelectionWriter();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(acceptorDispatch);
	}

}
