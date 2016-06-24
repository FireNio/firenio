package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class IOEventHandleAdaptor implements IOEventHandle {
	
	private Logger logger = LoggerFactory.getLogger(IOEventHandleAdaptor.class);

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		logger.info("exception,{}",cause);
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		logger.info("exception,{}",cause);
	}

	public void futureSent(Session session, WriteFuture future) {
		logger.info("future sent,{}",future);
	}

	public void accept(Session session, ReadFuture future) throws Exception {
		logger.info("future received,{}",future);
	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) {
		
	}
}
