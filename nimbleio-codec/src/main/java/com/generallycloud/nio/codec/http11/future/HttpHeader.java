package com.generallycloud.nio.codec.http11.future;

public class HttpHeader {

	private String name;
	
	private String value;

	protected HttpHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
