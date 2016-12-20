/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.codec.http11.future;

import java.util.Map;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;

public class ClientHttpReadFuture extends AbstractHttpReadFuture {

	public ClientHttpReadFuture(SocketChannelContext context, String url, String method) {
		super(context);
		this.method = method;
		this.setRequestURL(url);
	}

	public ClientHttpReadFuture(SocketSession session, ByteBuf readBuffer) {
		super(session, readBuffer);
	}

	@Override
	protected void setDefaultResponseHeaders(Map<String, String> headers) {
		headers.put("Connection", "keep-alive");
	}

	@Override
	public void updateWebSocketProtocol() {
		session.setProtocolFactory(PROTOCOL_FACTORY);
		session.setProtocolDecoder(WEBSOCKET_PROTOCOL_DECODER);
		session.setProtocolEncoder(WEBSOCKET_PROTOCOL_ENCODER);
	}

	@Override
	protected void parseContentType(String contentType) {

		if (!StringUtil.isNullOrBlank(contentType)) {

			if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

				this.contentType = CONTENT_APPLICATION_URLENCODED;

			} else if (contentType.startsWith("multipart/form-data;")) {

				int index = KMP_BOUNDARY.match(contentType);

				if (index != -1) {
					this.boundary = contentType.substring(index + 9);
				}

				this.contentType = CONTENT_TYPE_MULTIPART;
			} else {
				// FIXME other content-type
			}
		} else {
			this.contentType = CONTENT_APPLICATION_URLENCODED;
		}
	}

	@Override
	protected void parseFirstLine(String line) {
		String[] array = line.split(" ");
		this.version = array[0];
		this.status = HttpStatus.getHttpStatus(Integer.parseInt(array[1]));
	}
}
