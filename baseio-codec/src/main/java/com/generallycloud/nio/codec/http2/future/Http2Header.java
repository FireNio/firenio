package com.generallycloud.nio.codec.http2.future;

public class Http2Header {

	public Http2Header(String name, String value) {
		this(0, name, value);
	}

	public Http2Header(int index, String name, String value) {
		this.index = index;
		this.name = name;
		this.value = value;
		this.size = sizeOf(name, value);
	}

	private final int		index;
	private final String	name;
	private final String	value;
	private final int		size;

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int size() {
		return size;
	}

	public static int sizeOf(String name, String value) {
		return name.length() + value.length();
	}
}
