package com.gifisan.nio.client;

import java.io.IOException;
import java.nio.charset.Charset;

import com.gifisan.nio.component.AbstractProtocolDecoder;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.component.ProtocolDecoder;

public class ClientProtocolDecoder extends AbstractProtocolDecoder implements ProtocolDecoder {


	public void gainNecessary(EndPoint endPoint, ProtocolData data, Charset charset, byte[] header) throws IOException {
		//
	}

	
}
