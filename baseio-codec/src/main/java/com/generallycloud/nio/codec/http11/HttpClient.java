package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;

public class HttpClient {

	private BaseContext			context;

	private Session			session;

	private HttpIOEventHandle	ioEventHandle;

	public HttpClient(Session session) {
		this.session = session;
		this.context = session.getContext();
		this.ioEventHandle = (HttpIOEventHandle) context.getIOEventHandleAdaptor();
	}
	
	public synchronized HttpReadFuture request(HttpReadFuture future,long timeout) throws IOException {
		
		Waiter<HttpReadFuture> waiter = new Waiter<HttpReadFuture>();

		ioEventHandle.setWaiter(waiter);

		session.flush(future);

		if (waiter.await(timeout)) {
			throw new TimeoutException("timeout");
		}

		return waiter.getPayload();
	}

	public synchronized HttpReadFuture request(HttpReadFuture future) throws IOException {

		return request(future, 3000);
	}

}
