package com.gifisan.nio.connector;

import java.nio.channels.Selector;

import com.gifisan.nio.acceptor.ServerEndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;

public class ClientTCPSelectorLoop extends TCPSelectorLoop {

	public ClientTCPSelectorLoop(NIOContext context, Selector selector, TCPConnector connector,
			ServerEndPointWriter endPointWriter)  {
		
		super(context, selector, endPointWriter);
		
		this._alpha_acceptor = new TCPSelectionConnector(context, selector, connector, endPointWriter);
	}

}
