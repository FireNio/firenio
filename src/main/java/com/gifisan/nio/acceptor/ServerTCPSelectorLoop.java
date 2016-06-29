package com.gifisan.nio.acceptor;

import java.nio.channels.Selector;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;

public class ServerTCPSelectorLoop extends TCPSelectorLoop {

	public ServerTCPSelectorLoop(NIOContext context, Selector selector, EndPointWriter endPointWriter) {
		
		super(context, selector, endPointWriter);
		
		this._alpha_acceptor = new TCPSelectionAcceptor(context, endPointWriter, selector);
	}

}
