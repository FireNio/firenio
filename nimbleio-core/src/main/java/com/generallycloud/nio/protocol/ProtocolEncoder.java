package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.IOSession;

public interface ProtocolEncoder {

	/**
	 * 注意：encode失败要release掉encode过程中申请的内存
	 * @param channel
	 * @param future
	 * @return
	 * @throws IOException
	 */
	public abstract IOWriteFuture encode(IOSession session,IOReadFuture future) throws IOException;

}
