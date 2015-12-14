package com.gifisan.mtp.server;

import java.io.IOException;


public interface InnerResponse extends Response{

	public void finish() throws IOException;
	
}
