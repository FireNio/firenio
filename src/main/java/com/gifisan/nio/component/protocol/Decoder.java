package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOReadFuture;

public interface Decoder {
	
	public abstract IOReadFuture decode(EndPoint endPoint,byte[] header) throws IOException;
	
}
