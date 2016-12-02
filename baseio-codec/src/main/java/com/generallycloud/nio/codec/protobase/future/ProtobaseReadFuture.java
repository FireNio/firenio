package com.generallycloud.nio.codec.protobase.future;

import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ParametersReadFuture;

public interface ProtobaseReadFuture extends HashedBalanceReadFuture, NamedReadFuture , ParametersReadFuture{

	public abstract int getTextLength();

	public abstract int getBinaryLength();

	public abstract boolean hasBinary();

	public abstract byte[] getBinary();

	public abstract Integer getFutureID();

	public abstract BufferedOutputStream getWriteBinaryBuffer();

	public abstract void writeBinary(byte b);

	public abstract void writeBinary(byte[] bytes);

	public abstract void writeBinary(byte[] bytes, int offset, int length);
}
