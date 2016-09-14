package com.generallycloud.nio.component.protocol.nio.future;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.protocol.BalanceReadFuture;

public interface NIOReadFuture extends BalanceReadFuture {

	public abstract String getText();

	public abstract int getTextLength();

	public abstract Parameters getParameters();

	public abstract int getBinaryLength();

	public abstract boolean hasBinary();

	public abstract byte[] getBinary();

	public abstract BufferedOutputStream getWriteBinaryBuffer();

	public abstract void writeBinary(byte b);

	public abstract void writeBinary(byte[] bytes);

	public abstract void writeBinary(byte[] bytes, int offset, int length);
}
