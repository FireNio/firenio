package com.generallycloud.nio.component;

public interface HeapOutputStream {

	public abstract int size();

	public abstract byte[] toByteArray();

	public abstract byte[] array();

}
