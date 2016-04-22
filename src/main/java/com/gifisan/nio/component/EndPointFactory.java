package com.gifisan.nio.component;

import java.nio.channels.SelectionKey;

public interface EndPointFactory {
	
	public abstract EndPoint getEndPoint(SelectionKey selectionKey);

}
