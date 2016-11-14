package com.generallycloud.nio.component;

import com.generallycloud.nio.buffer.ByteBuf;

public interface ChannelByteBufReader {

	public abstract void accept(SocketChannel channel,ByteBuf buf) throws Exception;
	
}
