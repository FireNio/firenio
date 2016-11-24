package com.generallycloud.nio;

import java.io.IOException;

@SuppressWarnings("serial")
public class FlushedException extends IOException{

	public FlushedException(String string) {
		super(string);
	}
}
