package com.generallycloud.nio.extend;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class SimpleIOEventHandle extends IoEventHandleAdaptor {

	private Map<String, OnReadFutureWrapper>	listeners	= new HashMap<String, OnReadFutureWrapper>();

	public void accept(Session session, ReadFuture future) throws Exception {

		NamedReadFuture f = (NamedReadFuture) future;

		OnReadFutureWrapper onReadFuture = listeners.get(f.getFutureName());

		if (onReadFuture != null) {
			onReadFuture.onResponse(session, f);
		}
	}
	
	public void listen(String serviceName, OnReadFuture onReadFuture) throws IOException {

		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		OnReadFutureWrapper wrapper = listeners.get(serviceName);

		if (wrapper == null) {

			wrapper = new OnReadFutureWrapper();

			listeners.put(serviceName, wrapper);
		}

		if (onReadFuture == null) {
			return;
		}

		wrapper.setListener(onReadFuture);
	}
	
	public OnReadFutureWrapper getOnReadFutureWrapper(String serviceName){
		return listeners.get(serviceName);
	}
	
	public void putOnReadFutureWrapper(String serviceName,OnReadFutureWrapper wrapper){
		listeners.put(serviceName, wrapper);
	}
}
