package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class SocketChannelSelectionReader implements SelectionAcceptor {
	
	private IOReadFutureAcceptor	ioReadFutureAcceptor;

	public SocketChannelSelectionReader(NIOContext context) {
		this.ioReadFutureAcceptor = context.getIOReadFutureAcceptor();
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel == null || !channel.isOpened()) {
			//该channel已经被关闭
			return;
		}

		IOReadFuture future = channel.getReadFuture();

		if (future == null) {
			
			ProtocolDecoder decoder = channel.getProtocolDecoder();

			future = decoder.decode(channel);

			if (future == null) {
				CloseUtil.close(channel);
				return;
			}

			channel.setReadFuture(future);
		}

		try {
			if (!future.read()) {

				return;
			}
			
			ReleaseUtil.release(future);
			
		} catch (Throwable e) {
			
			ReleaseUtil.release(future);
			
			if (e instanceof IOException) {
				throw (IOException)e;
			}
			
			throw new IOException("exception occurred when read from channel,the nested exception is,"+e.getMessage(),e);
		}

		channel.setReadFuture(null);

		Session session = channel.getSession();
		
		session.active();
		
		ioReadFutureAcceptor.accept(session, future);
	}

}
