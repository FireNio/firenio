package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.List;

import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public class NIOSelectionWriter extends AbstractNIOSelection implements SelectionAcceptor{
	
	public NIOSelectionWriter(NIOContext context) {
		super(context);
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;
		
		EndPoint endPoint = getEndPoint(context,selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}
		
		List<IOWriteFuture> writers = endPoint.getWriter();
		
		EndPointWriter endPointWriter = context.getEndPointWriter();
		
		for(IOWriteFuture writer:writers){
			endPointWriter.offer(writer);
		}
		
		writers.clear();
		
	}

	protected EndPoint getEndPoint(NIOContext context, SelectionKey selectionKey) throws SocketException {
		return (EndPoint) selectionKey.attachment();
	}
	
}
