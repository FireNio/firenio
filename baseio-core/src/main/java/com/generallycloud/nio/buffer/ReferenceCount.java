package com.generallycloud.nio.buffer;

public class ReferenceCount {

	private int	referenceCount = 1;

	public int increament() {
		return ++referenceCount;
	}

	public int deincreament() {
		return --referenceCount;
	}

	public int getReferenceCount() {
		return referenceCount;
	}
}
