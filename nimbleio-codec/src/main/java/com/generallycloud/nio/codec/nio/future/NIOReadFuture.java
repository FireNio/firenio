package com.generallycloud.nio.codec.nio.future;

import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.protocol.NamedReadFuture;

public interface NIOReadFuture extends HashedBalanceReadFuture,NamedReadFuture {

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
