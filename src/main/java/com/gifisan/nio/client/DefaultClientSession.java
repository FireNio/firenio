package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.AbstractSession;
import com.gifisan.nio.component.ClientProtocolEncoder;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.service.WriteFuture;

public class DefaultClientSession extends AbstractSession implements ClientSesssion {

	private MessageBus		bus		= null;
	private ClientContext	context	= null;
	private ClientProtocolEncoder encoder = null;
	private EndPointWriter endPointWriter = null;

	public DefaultClientSession(EndPoint endPoint, byte sessionID, MessageBus bus, ClientContext context) {
		super(endPoint, sessionID);
		this.bus = bus;
		this.context = context;
		this.encoder = context.getProtocolEncoder();
		this.endPointWriter = context.getEndPointWriter();
	}


	public void request(String serviceName, String content) throws IOException {
		request(serviceName, content, null);
	}

	public void request(String serviceName, String content, InputStream inputStream) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		byte [] array = content == null? null : content.getBytes(context.getEncoding());
		
		WriteFuture future = encoder.encode(this, array, inputStream, handle);
		
		this.endPointWriter.offer(future);
	}

	public void offer(ReadFuture response) {
		this.bus.offer(response);
	}

	public ClientContext getContext() {
		return context;
	}

	public ReadFuture poll(long timeout) {
		return bus.poll(timeout);
	}
}
