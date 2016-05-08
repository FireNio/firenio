package com.gifisan.nio.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOEndPoint;
import com.gifisan.nio.component.SelectionAcceptor;
import com.gifisan.nio.component.SelectorManagerLoop;
import com.gifisan.nio.server.NIOContext;

public class ClientSelectorManagerLoop extends SelectorManagerLoop implements SelectionAcceptor, Runnable {

	private Logger				logger			= LoggerFactory.getLogger(ClientSelectorManagerLoop.class);

	public ClientSelectorManagerLoop(NIOContext context, Selector selector,EndPointWriter endPointWriter) {
		super(context, selector,endPointWriter);
	}

	protected void acceptException(SelectionKey selectionKey, IOException exception) {

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {

			EndPoint endPoint = (EndPoint) attachment;
			
			endPoint.endConnect();

			CloseUtil.close(endPoint);
		}

		logger.error(exception.getMessage(), exception);
	}

	private boolean isEndPoint(Object object) {
		return object != null && (object.getClass() == NIOEndPoint.class || object instanceof EndPoint);
	}

}
