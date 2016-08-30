package com.generallycloud.nio.component;

import java.nio.channels.SelectionKey;

public interface SelectionAcceptor {

	public void accept(SelectionKey selectionKey) throws Exception;
	
}
