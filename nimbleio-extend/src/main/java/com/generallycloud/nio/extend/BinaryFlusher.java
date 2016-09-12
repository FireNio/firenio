package com.generallycloud.nio.extend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class BinaryFlusher {

	private InputStream	inputStream;

	private Session	session;

	private int		time;

	private int		current_time;

	private byte[]		cache	= new byte[1024 * 8];

	private String		serviceName;

	private Map		params;

	public BinaryFlusher(InputStream inputStream, Session session, String serviceName, Map params) {
		this.inputStream = inputStream;
		this.session = session;
		this.serviceName = serviceName;
		this.params = params;
	}

	public void flush() throws IOException {

		NIOContext context = session.getContext();

		final IOEventHandleAdaptor _Adaptor = context.getIOEventHandleAdaptor();

		IOEventHandleAdaptor adaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				_Adaptor.accept(session, future);
			}

			public void futureSent(Session session, ReadFuture future) {

				current_time++;

				if (current_time == time) {
					return;
				}

				session.getEventLoop().dispatch(new Runnable() {

					public void run() {
						try {
							BinaryFlusher.this.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}

			public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
				_Adaptor.exceptionCaught(session, future, cause, state);
			}
		};

		if (time == 0) {

			NIOReadFuture readFuture = ReadFutureFactory.create(session, serviceName, adaptor);

			int available = inputStream.available();

			params.put("available", available);

			readFuture.write(JSONObject.toJSONString(params));

			time = (available + 1024 * 8 - 1) / 1024 * 8;

			int size = inputStream.read(cache);

			readFuture.writeBinary(cache, 0, size);

		} else {

			NIOReadFuture readFuture = ReadFutureFactory.create(session, serviceName, adaptor);

			int size = inputStream.read(cache);

			readFuture.writeBinary(cache, 0, size);
		}
	}

}
