/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public interface SelectorLoop extends SelectionAcceptor, EventLoopThread {

	public abstract Selector buildSelector(SelectableChannel channel) throws IOException;

	public abstract Selector getSelector();

	public abstract boolean isMainSelector();

	@Override
	public abstract void accept(SelectionKey key);
	
	public abstract ChannelContext getContext();

	public abstract SelectableChannel getSelectableChannel();

	public abstract ByteBufAllocator getByteBufAllocator();

	public abstract void wakeup();

	public abstract void fireEvent(SelectorLoopEvent event);

	public abstract boolean isWaitForRegist();

	public abstract void setWaitForRegist(boolean isWaitForRegist);

	public abstract ReentrantLock getIsWaitForRegistLock();

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
