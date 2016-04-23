package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.ErrorReadFuture;
import com.gifisan.nio.service.Future;
import com.gifisan.nio.service.WriteFuture;

public class ClientIOExceptionHandle implements IOExceptionHandle{

	public void handle(Session session, Future future, IOException e) {
		DefaultClientSession clientSesssion = (DefaultClientSession) session;
		
		WriteFuture writeFuture = (WriteFuture) future;
		
		ErrorReadFuture eFuture = new ErrorReadFuture(
				future.getServiceName(), 
				future.getText(), 
				writeFuture.getInputStream(), e);
		clientSesssion.offer(eFuture);
	}
	
}
