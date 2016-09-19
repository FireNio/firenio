package com.generallycloud.nio.common;

import java.io.Closeable;
import java.nio.channels.Selector;

import com.generallycloud.nio.acceptor.IOAcceptor;

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
	
	public static void unbind(IOAcceptor acceptor){
		if (acceptor == null) {
			return;
		}
		try {
			acceptor.unbind();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
