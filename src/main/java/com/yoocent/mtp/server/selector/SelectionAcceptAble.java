package com.yoocent.mtp.server.selector;

import java.nio.channels.SelectionKey;

public interface SelectionAcceptAble {

	public void accept(SelectionKey selectionKey) throws Exception;
	
}
