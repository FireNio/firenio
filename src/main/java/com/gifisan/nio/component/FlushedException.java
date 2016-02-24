package com.gifisan.nio.component;

import java.io.IOException;

class FlushedException extends IOException{

	public FlushedException(String string) {
		super(string);
	}
}
