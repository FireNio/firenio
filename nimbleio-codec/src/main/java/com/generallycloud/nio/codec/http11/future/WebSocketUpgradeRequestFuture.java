package com.generallycloud.nio.codec.http11.future;

public class WebSocketUpgradeRequestFuture extends ClientHttpReadFuture {

	public WebSocketUpgradeRequestFuture(String url) {
		super(url, "GET");

		this.setResponseHeaders();
	}

	private void setResponseHeaders() {
		setResponseHeader("Connection", "Upgrade");
		setResponseHeader("Upgrade", "websocket");
		setResponseHeader("Sec-WebSocket-Version", "13");
		setResponseHeader("Sec-WebSocket-Key", "VR+OReqwhymoQ21dBtoIMQ==");
		setResponseHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
	}
}
