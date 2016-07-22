package com.gifisan.nio.extend.plugin.http.client;

import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public class HttpIOEventHandle extends IOEventHandleAdaptor{
	
	private HttpClient httpClient = new HttpClient();

	public void accept(Session session, ReadFuture future) throws Exception {
		httpClient.getListener().onResponse(session, future);
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	
}
