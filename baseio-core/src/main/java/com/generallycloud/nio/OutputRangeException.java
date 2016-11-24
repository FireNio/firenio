package com.generallycloud.nio;

import java.io.IOException;

@SuppressWarnings("serial")
public class OutputRangeException extends IOException{

	public OutputRangeException(String string) {
		super(string);
	}

}
