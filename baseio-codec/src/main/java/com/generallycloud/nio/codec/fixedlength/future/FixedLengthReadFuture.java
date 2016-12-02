package com.generallycloud.nio.codec.fixedlength.future;

import com.generallycloud.nio.protocol.ReadFuture;

public interface FixedLengthReadFuture extends ReadFuture{

	public abstract byte[] getByteArray();
}
