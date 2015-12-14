package com.gifisan.mtp.server.selector;

import java.nio.channels.SelectionKey;

public interface SelectionAccept {

	public void accept(SelectionKey selectionKey) throws Exception;
	
}
