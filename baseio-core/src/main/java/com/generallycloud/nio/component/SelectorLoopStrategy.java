package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;

public interface SelectorLoopStrategy{

	public abstract void loop(SelectorLoop looper) throws IOException;
	
	public abstract void fireEvent(SelectorLoopEvent event);
	
	public abstract SocketChannel buildSocketChannel(SelectionKey selectionKey,SelectorLoop selectorLoop) throws SocketException ;
	
	public abstract void regist(java.nio.channels.SocketChannel channel,SelectorLoop selectorLoop) throws IOException;
	
	public abstract void stop();
}
