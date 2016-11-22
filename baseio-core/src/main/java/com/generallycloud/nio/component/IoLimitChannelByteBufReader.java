package com.generallycloud.nio.component;

import com.generallycloud.nio.buffer.ByteBuf;

public class IoLimitChannelByteBufReader extends LinkableChannelByteBufReader{

	public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {

		getNext().getValue().accept(channel, buffer);
	}

}
