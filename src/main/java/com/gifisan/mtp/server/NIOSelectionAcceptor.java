package com.gifisan.mtp.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.NIOSelectionReader;
import com.gifisan.mtp.component.NIOSelectionWriter;
import com.gifisan.mtp.component.ServerNIOEndPoint;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.concurrent.BlockingQueueThreadPool;
import com.gifisan.mtp.concurrent.ThreadPool;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class NIOSelectionAcceptor extends AbstractLifeCycle implements SelectionAcceptor{
	
	private SelectionAccept [] 		acceptors 			= new SelectionAccept[5];
	private ServletService 			service 				= null;
	private ThreadPool 				servletThreadPool 		= null;
	private ServletContext			context				= null;
//	private BlockingQueueThreadPool	selectionThreadPool  	= null;

	public NIOSelectionAcceptor(ServletContext context) {
		this.context = context;
	}
	

	public void accept(SelectionKey selectionKey) throws IOException {
		
		int opt = selectionKey.readyOps();
			
		acceptors[opt].accept(selectionKey);
	}
	
	public boolean isEndPoint(Object object){
		if (object == null) {
			return false;
		}
		
		return object.getClass() == ServerNIOEndPoint.class || object instanceof EndPoint;
		
	}
	
	protected void doStart() throws Exception {
		SharedBundle bundle 		= SharedBundle.instance();
		ServletContext context 		= this.context;
		int CORE_SIZE 				= bundle.getIntegerProperty("SERVER.CORE_SIZE",4);
		this.service             	= new ServletService(context);
//		this.selectionThreadPool  	= new BlockingQueueThreadPool("Selection-accept-job",  CORE_SIZE);
		this.servletThreadPool   	= new BlockingQueueThreadPool("Servlet-accept-Job",  CORE_SIZE);
//		this.servletThreadPool   	= new LinkNodeQueueThreadPool("Servlet-job",  CORE_SIZE);
		this.service           		.start();
//		this.selectionThreadPool		.start();
		this.servletThreadPool 		.start();
		this.acceptors[1] = new NIOSelectionReader(context,service,servletThreadPool);
		this.acceptors[4] = new NIOSelectionWriter();
	}
	
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(service);
		LifeCycleUtil.stop(servletThreadPool);
//		LifeCycleUtil.stop(selectionThreadPool);
	}

}
