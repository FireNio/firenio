package com.generallycloud.nio.common;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

	public static void read(ByteBuffer dest, ByteBuffer src) {

		int srcRemaing = src.remaining();

		if (srcRemaing == 0) {
			return;
		}

		int remaining = dest.remaining();

		if (remaining == 0) {
			return;
		}

		if (remaining <= srcRemaing) {

			dest.put(src.array(), src.position(), remaining);

			src.position(src.position() + remaining);
			
		} else {

			dest.put(src.array(), src.position(), srcRemaing);

			src.position(src.limit());
		}
	}

}
