package com.gifisan.mtp.common;

import java.io.Closeable;

public class CloseUtil {

	public static void close(Closeable closeable){
		if (closeable == null) {
			return ;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
