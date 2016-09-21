package com.generallycloud.nio.component.protocol.nio.future;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.protocol.BalanceReadFuture;
import com.generallycloud.nio.component.protocol.NamedReadFuture;

public interface NIOReadFuture extends BalanceReadFuture,NamedReadFuture {

	public abstract String getText();

	public abstract int getTextLength();

	public abstract Parameters getParameters();

	public abstract int getBinaryLength();

	public abstract boolean hasBinary();

	public abstract byte[] getBinary();
	
	public abstract Integer getFutureID();

	public abstract BufferedOutputStream getWriteBinaryBuffer();

	public abstract void writeBinary(byte b);

	public abstract void writeBinary(byte[] bytes);

	public abstract void writeBinary(byte[] bytes, int offset, int length);
}
