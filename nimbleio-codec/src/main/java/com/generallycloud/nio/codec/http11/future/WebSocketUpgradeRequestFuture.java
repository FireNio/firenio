package com.generallycloud.nio.codec.http11.future;

public class WebSocketUpgradeRequestFuture extends HttpRequestFutureImpl {

	public WebSocketUpgradeRequestFuture(String url) {
		super(url, "GET");

		this.setHeaders();
	}

	private void setHeaders() {
		setHeader("Connection", "Upgrade");
		setHeader("Upgrade", "websocket");
		setHeader("Sec-WebSocket-Version", "13");
		setHeader("Sec-WebSocket-Key", "VR+OReqwhymoQ21dBtoIMQ==");
		setHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
	}

}
