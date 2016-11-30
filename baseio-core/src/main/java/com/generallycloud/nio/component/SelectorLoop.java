package com.generallycloud.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public interface SelectorLoop extends SelectionAcceptor, EventLoopThread {

	public abstract Selector buildSelector(SelectableChannel channel) throws IOException;

	public abstract Selector getSelector();

	public abstract boolean isMainSelector();

	public abstract void accept(SelectionKey key);
	
	public abstract ChannelContext getContext();

	public abstract SelectableChannel getSelectableChannel();

	public abstract ByteBufAllocator getByteBufAllocator();

	public abstract void wakeup();

	public abstract void fireEvent(SelectorLoopEvent event);

	public abstract boolean isWaitForRegist();

	public abstract void setWaitForRegist(boolean isWaitForRegist);

	public abstract byte[] getIsWaitForRegistLock();

	public abstract void setMainSelector(boolean isMainSelector);

	public interface SelectorLoopEvent extends Closeable {

		/**
		 * 返回该Event是否结束
		 */
		boolean handle(SelectorLoop selectLoop) throws IOException;
		
		boolean isPositive();
	}

	public abstract void rebuildSelector();
	
	public abstract SelectorLoopStrategy getSelectorLoopStrategy();

	public abstract SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException;

}
