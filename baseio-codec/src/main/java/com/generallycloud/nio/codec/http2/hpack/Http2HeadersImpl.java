package com.generallycloud.nio.codec.http2.hpack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Http2HeadersImpl implements Http2Headers {

	private HashMap<String, String>	headers	= new HashMap<String, String>();

	private String					method;
	private String					scheme;
	private String					authority;
	private String					path;
	private String					status;

	public Iterator<Entry<String, String>> iterator() {
		return headers.entrySet().iterator();
	}

	public Http2Headers method(String value) {
		this.method = value;
		this.headers.put(":method", value);
		return this;
	}

	public Http2Headers scheme(String value) {
		this.scheme = value;
		this.headers.put(":scheme", value);
		return this;
	}

	public Http2Headers authority(String value) {
		this.authority = value;
		this.headers.put(":authority", value);
		return this;
	}

	public Http2Headers path(String value) {
		this.path = value;
		this.headers.put(":path", value);
		return this;
	}

	public Http2Headers status(String value) {
		this.status = value;
		this.headers.put(":status", value);
		return this;
	}

	public String method() {
		return method;
	}

	public String scheme() {
		return scheme;
	}

	public String authority() {
		return authority;
	}

	public String path() {
		return path;
	}

	public String status() {
		return status;
	}

	public void add(String name, String value) {
		this.headers.put(name, value);
	}

}
