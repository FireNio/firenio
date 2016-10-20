package com.generallycloud.nio.codec.http11.future;

public class HttpHeader {

	private String name;
	
	private String value;
	
	private String key;

	protected HttpHeader(String name, String value) {
		this.name = name;
		this.value = value;
		this.key = name.toLowerCase();
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getKey() {
		return key;
	}
	
}
