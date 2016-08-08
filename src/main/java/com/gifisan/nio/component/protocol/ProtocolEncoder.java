package com.gifisan.nio.component.protocol;

import java.io.IOException;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;
import com.gifisan.nio.component.protocol.future.ReadFuture;

/**
 * <pre>
 * [0       ~              11]
 *  0         = 类型 [0=TEXT，1=STREAM，2=MULTI]
 *  1,2,3     = request id的长度
 *  4,        = service name的长度
 *  5,6,7     = text content的长度
 *  8,9,10,11 = stream content的长度
 * </pre>
 * 
 */
public interface ProtocolEncoder {

	public abstract IOWriteFuture encode(TCPEndPoint endPoint,ReadFuture readFuture) throws IOException;

}
