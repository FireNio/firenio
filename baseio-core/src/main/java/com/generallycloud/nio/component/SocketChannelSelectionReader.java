package com.generallycloud.nio.component;

import java.nio.channels.SelectionKey;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class SocketChannelSelectionReader implements SelectionAcceptor {

	private ByteBuf			buf			= null;

	private ChannelByteBufReader	byteBufReader	= null;

	private Logger				logger		= LoggerFactory.getLogger(SocketChannelSelectionReader.class);

	public SocketChannelSelectionReader(BaseContext context) {
		int readBuffer = context.getServerConfiguration().getSERVER_READ_BUFFER();
		this.byteBufReader = context.getChannelByteBufReader();
		this.buf = UnpooledByteBufAllocator.getInstance().allocate(readBuffer);// FIXME 使用direct
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel == null || !channel.isOpened()) {
//			logger.info("closed selection key={}", selectionKey);
			// 该channel已经被关闭
			return;
		}

		ByteBuf buf = this.buf;

		buf.clear();

		buf.nioBuffer();

		int length = buf.read(channel);

		if (length == -1) {
			CloseUtil.close(channel);
			return;
		} else if (length == 0) {
			logger.info("read length 0, selection key={}", selectionKey);
			return;
		}

		channel.active();

		byteBufReader.accept(channel, buf.flip());

	}

}
