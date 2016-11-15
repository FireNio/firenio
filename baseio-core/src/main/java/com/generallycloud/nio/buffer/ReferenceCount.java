package com.generallycloud.nio.buffer;

public class ReferenceCount {

	private int	referenceCount = 0;

	public int increament() {
		return ++referenceCount;
	}

	public int deincreament() {
		return --referenceCount;
	}

	public int getReferenceCount() {
		return referenceCount;
	}

	public String toString() {
		return "ref="+referenceCount;
	}
	
	
}
