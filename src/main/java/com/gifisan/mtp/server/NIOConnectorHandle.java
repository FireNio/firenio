package com.gifisan.mtp.server;

import java.nio.channels.SelectionKey;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.BlockingQueueThreadPool;
import com.gifisan.mtp.component.NIOConnectorEndPoint;
import com.gifisan.mtp.component.NIOSelectionReader;
import com.gifisan.mtp.component.NIOSelectionWriter;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.component.ThreadPool;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class NIOConnectorHandle extends AbstractLifeCycle implements ConnectorHandle{
	
	private SelectionAccept [] acceptors 					= new SelectionAccept[5];
	private ServletService service 							= null;
	private ThreadPool servletThreadPool 					= null;
//	private BlockingQueueThreadPool selectionThreadPool  	= null;

	public void accept(SelectionKey selectionKey) throws Exception {
		
		
//		InnerEndPoint endPoint = getEndPoint(selectionKey);
//		
//		if (endPoint.accepting()) {
//			return ;
//		}
//		
//		if (endPoint.inStream()) {
//			synchronized (endPoint) {
//
//				endPoint.notify();
//				return;
//			}
//		} else {
//			endPoint.setAccepting(true);
//		}
		
		int opt = selectionKey.readyOps();
		
//		SelectionAcceptJob selectionAcceptJob = new SelectionAcceptJobImpl(acceptors[opt],selectionKey);
//		
//		selectionKey.cancel();
		
//		selectionThreadPool.dispatch(selectionAcceptJob);
		
		acceptors[opt].accept(selectionKey);
	}
	
	protected boolean isEndPoint(Object object){
		if (object == null) {
			return false;
		}
		
		return object.getClass() == NIOConnectorEndPoint.class || object instanceof EndPoint;
		
	}
	
	protected void doStart() throws Exception {
		ServletContext context = ServletContextFactory.getServletContext();
		int APP_SERVER_CORE_SIZE 	= SharedBundle.getIntegerProperty("APP_SERVER_CORE_SIZE");
		APP_SERVER_CORE_SIZE = APP_SERVER_CORE_SIZE == 0 ? 4 :APP_SERVER_CORE_SIZE;
		this.service             	= new ServletService(context);
//		this.selectionThreadPool  	= new BlockingQueueThreadPool("Selection-job",  APP_SERVER_CORE_SIZE);
		this.servletThreadPool   	= new BlockingQueueThreadPool("Servlet-job",  APP_SERVER_CORE_SIZE);
//		this.servletThreadPool   	= new LinkNodeQueueThreadPool("Servlet-job",  APP_SERVER_CORE_SIZE);
		this.service           		.start();
//		this.selectionThreadPool	.start();
		this.servletThreadPool 		.start();
		this.acceptors[1] = new NIOSelectionReader(context,servletThreadPool,service);
		this.acceptors[4] = new NIOSelectionWriter();
	}
	
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(service);
		LifeCycleUtil.stop(servletThreadPool);
//		LifeCycleUtil.stop(selectionThreadPool);
	}

}
