package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;

public class TCPSelectionReader implements SelectionAcceptor {
	
	private IOReadFutureAcceptor	ioReadFutureAcceptor;

	public TCPSelectionReader(NIOContext context) {
		this.ioReadFutureAcceptor = context.getIOReadFutureAcceptor();
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel endPoint = (SocketChannel) selectionKey.attachment();

		if (endPoint == null || !endPoint.isOpened()) {
			//该EndPoint已经被关闭
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {
			
			ProtocolDecoder decoder = endPoint.getProtocolDecoder();

			future = decoder.decode(endPoint);

			if (future == null) {
				CloseUtil.close(endPoint);
				return;
			}

			endPoint.setReadFuture(future);
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

		endPoint.setReadFuture(null);

		Session session = endPoint.getSession();
		
		session.active();
		
		ioReadFutureAcceptor.accept(session, future);
	}

}
