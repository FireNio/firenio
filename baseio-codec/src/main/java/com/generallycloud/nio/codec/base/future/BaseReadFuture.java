package com.generallycloud.nio.codec.base.future;

import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.TextReadFuture;

public interface BaseReadFuture extends HashedBalanceReadFuture, NamedReadFuture, TextReadFuture {

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
