package com.gifisan.nio;

import java.io.IOException;

public class FlushedException extends IOException{

	public FlushedException(String string) {
		super(string);
	}
}
