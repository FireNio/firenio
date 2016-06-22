package com.gifisan.nio.component.protocol;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.IOWriteFuture;

/**
 * <pre>
 * [0       ~              11]
 *  0         = 类型 [3=心跳，0=TEXT，1=STREAM，2=MULTI]
 *  1,2,3     = request id的长度
 *  4,        = service name的长度
 *  5,6,7     = text content的长度
 *  8,9,10,11 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolEncoder {

	public abstract IOWriteFuture encode(TCPEndPoint endPoint, int request_id, String service_name, byte[] text_array,
			InputStream inputStream) throws IOException;

}
