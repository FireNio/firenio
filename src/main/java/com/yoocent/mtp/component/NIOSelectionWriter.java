package com.yoocent.mtp.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.yoocent.mtp.server.selector.SelectionAcceptAble;

public class NIOSelectionWriter implements SelectionAcceptAble{

	public void accept(SelectionKey selectionKey) throws Exception {
		throw new IOException("print a will");
	}
}
