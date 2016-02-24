package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.selector.SelectionAccept;

public class NIOSelectionWriter implements SelectionAccept{

	public void accept(SelectionKey selectionKey) throws IOException {
		throw new IOException("print a will");
	}
}
