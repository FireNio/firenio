package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.NIOSelectionReader;
import com.gifisan.nio.component.NIOSelectionWriter;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public class NIOSelectionAcceptor implements SelectionAcceptor {

	private SelectionAcceptor[]	acceptors	= new SelectionAcceptor[5];

	public NIOSelectionAcceptor(NIOContext context) {
		this.acceptors[1] = new NIOSelectionReader(context);
		this.acceptors[4] = new NIOSelectionWriter(context);
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		int opt = selectionKey.readyOps();

		acceptors[opt].accept(selectionKey);
	}



}
