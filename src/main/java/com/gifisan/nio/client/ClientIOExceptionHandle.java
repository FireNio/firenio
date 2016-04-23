package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.Future;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.WriteFuture;

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
