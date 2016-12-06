package com.generallycloud.nio.codec.linebased.future;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ReadFuture;

public interface LineBasedReadFuture extends ReadFuture{

	public static final byte	LINE_BASE	= '\n';
	
	public abstract BufferedOutputStream getLineOutputStream();
	
}
