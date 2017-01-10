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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;

public abstract class AbstractSelectorLoop extends AbstractEventLoop implements SelectorEventLoop {

	private static final Logger					logger				= LoggerFactory
			.getLogger(AbstractSelectorLoop.class);
	protected ByteBufAllocator					byteBufAllocator		= null;
	protected boolean							hasTask				= false;
	protected BufferedArrayList<SelectorLoopEvent>	negativeEvents			= new BufferedArrayList<SelectorLoopEvent>();
	protected BufferedArrayList<SelectorLoopEvent>	positiveEvents			= new BufferedArrayList<SelectorLoopEvent>();
	protected ReentrantLock						runLock				= new ReentrantLock();
	protected int								runTask				= 0;
	protected AtomicBoolean						selecting				= new AtomicBoolean();
	protected SocketSelector							selector				= null;

	protected AbstractSelectorLoop(ChannelContext context) {
		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	@Override
	public void dispatch(SelectorLoopEvent event) throws RejectedExecutionException {

		if (inEventLoop()) {

			if (!isRunning()) {
				CloseUtil.close(event);
				return;
			}

			handleEvent(event);

			return;
		}

		ReentrantLock lock = this.runLock;

		lock.lock();

		try {

			dispatch0(event);

		} finally {

			lock.unlock();
		}
	}

	protected void cancelSelectionKey(SocketChannel channel, Throwable t) {

		logger.error(t.getMessage() + " channel:" + channel, t);

		CloseUtil.close(channel);
	}

	private void dispatch0(SelectorLoopEvent event) {

		if (!isRunning()) {
			CloseUtil.close(event);
			return;
		}

		fireEvent(event);
	}

	@Override
	public void doStartup() throws IOException {
		rebuildSelector();
	}

	public void fireEvent(SelectorLoopEvent event) {

		positiveEvents.offer(event);

		if (positiveEvents.getBufferSize() < 3) {

			wakeup();
		}
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	@Override
	public SocketSelector getSelector() {
		return selector;
	}

	public void handleEvent(SelectorLoopEvent event) {

		try {

			if (!event.handle(this)) {
				return;
			}

			// FIXME xiaolv hui jiangdi
			if (event.isPositive()) {
				positiveEvents.offer(event);
			} else {
				negativeEvents.offer(event);
			}

		} catch (IOException e) {

			CloseUtil.close(event);
		}
	}

	private void handleEvents(List<SelectorLoopEvent> eventBuffer) {

		for (SelectorLoopEvent event : eventBuffer) {

			handleEvent(event);
		}
	}

	protected void handleNegativeEvents() {

		List<SelectorLoopEvent> eventBuffer = negativeEvents.getBuffer();

		if (eventBuffer.size() == 0) {
			return;
		}

		handleEvents(eventBuffer);
	}

	protected void handlePositiveEvents(boolean refresh) {

		List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

		if (eventBuffer.size() == 0) {

			hasTask = false;

			return;
		}

		handleEvents(eventBuffer);

		hasTask = positiveEvents.getBufferSize() > 0;

		if (hasTask && refresh) {
			runTask = 5;
		}

	}

	@Override
	public void rebuildSelector() throws IOException {
		this.selector = rebuildSelector0();
	}

	protected abstract SocketSelector rebuildSelector0() throws IOException;

	protected void selectEmpty(SelectorEventLoop looper, long last_select) {

		long past = System.currentTimeMillis() - last_select;

		if (past < 1000) {

			if (!looper.isRunning() || past < 0) {
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);

			try {
				looper.rebuildSelector();
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	@Override
	public void wakeup() {

		if (selecting.compareAndSet(false, true)) {
			selecting.set(false);
			return;
		}

		selector.wakeup();

		super.wakeup();
	}

}
