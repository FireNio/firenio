package com.gifisan.nio.component;

import java.nio.ByteBuffer;

public interface ProtocolEncoder {

	
	public abstract ByteBuffer encode(byte sessionID, byte[] textArray, int streamLength) ;
}
