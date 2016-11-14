package com.generallycloud.nio.codec.fixedlength.future;

import com.generallycloud.nio.protocol.TextReadFuture;

public interface FixedLengthReadFuture extends TextReadFuture{

	public abstract byte[] getByteArray();
}
