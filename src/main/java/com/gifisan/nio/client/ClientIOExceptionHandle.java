package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.DefaultIOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.Future;
import com.gifisan.nio.component.future.WriteFuture;

public class ClientIOExceptionHandle extends DefaultIOEventHandle{

	public void handle(Session session, Future future, IOException e) {
		ProtectedClientSession clientSession = (ProtectedClientSession) session;
		
		WriteFuture writeFuture = (WriteFuture) future;
		
		ErrorReadFuture eFuture = new ErrorReadFuture(
				future.getServiceName(), 
				future.getText(), 
				clientSession,
				writeFuture.getInputStream(), 
				e);
		clientSession.offer(eFuture);
	}
	
}
