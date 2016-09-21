package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.TCPEndPoint;

public interface ProtocolEncoder {

	/**
	 * 注意：encode失败要release掉encode过程中申请的内存
	 * @param endPoint
	 * @param future
	 * @return
	 * @throws IOException
	 */
	public abstract IOWriteFuture encode(TCPEndPoint endPoint,IOReadFuture future) throws IOException;

}
