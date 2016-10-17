package com.generallycloud.nio.codec.fixedlength.future;

import java.nio.charset.Charset;


public interface FixedLengthReadFuture {

	public abstract String getText();
	
	public abstract String getText(Charset encoding);

	public abstract byte[] getByteArray();
}
