package com.generallycloud.nio.buffer;

class ReferenceCount {

	protected int	referenceCount = 1;

	protected int increament() {
		return ++referenceCount;
	}

	protected int deincreament() {
		return --referenceCount;
	}

	protected int getReferenceCount() {
		return referenceCount;
	}
}
