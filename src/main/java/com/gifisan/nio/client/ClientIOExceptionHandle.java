package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.Future;
import com.gifisan.nio.component.future.WriteFuture;

public class ClientIOExceptionHandle implements IOExceptionHandle{

	public void handle(Session session, Future future, IOException e) {
		ProtectedClientSession clientSesssion = (ProtectedClientSession) session;
		
		WriteFuture writeFuture = (WriteFuture) future;
		
		ErrorReadFuture eFuture = new ErrorReadFuture(
				future.getServiceName(), 
				future.getText(), 
				clientSesssion,
				writeFuture.getInputStream(), 
				e);
		clientSesssion.offer(eFuture);
	}
	
}
