package com.gifisan.nio.common;

import java.io.Closeable;
import java.nio.channels.Selector;

public class CloseUtil {

	public static void close(Closeable closeable){
		if (closeable == null) {
			return ;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			DebugUtil.debug(e);
		}
	}
	
	public static void close(Selector selector){
		if (selector == null) {
			return ;
		}
		try {
			selector.close();
		} catch (Exception e) {
			DebugUtil.debug(e);
		}
	}
}
