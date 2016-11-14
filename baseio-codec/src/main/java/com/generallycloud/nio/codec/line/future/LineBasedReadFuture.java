package com.generallycloud.nio.codec.line.future;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.TextReadFuture;

public interface LineBasedReadFuture extends TextReadFuture{

	public static final byte	LINE_BASE	= '\n';
	
	public abstract BufferedOutputStream getLineOutputStream();
	
}
