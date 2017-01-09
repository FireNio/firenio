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
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;

public abstract class AbstractSelectorLoop extends AbstractEventLoop implements SelectorEventLoop {

	private boolean				isMainSelector			= false;
	private boolean				isWaitForRegist		= false;
	private ReentrantLock			isWaitForRegistLock		= new ReentrantLock();

	protected ByteBufAllocator		byteBufAllocator		= null;
	protected SelectableChannel		selectableChannel		= null;
	protected Selector				selector				= null;
	protected SelectorLoopStrategy	selectorLoopStrategy	= null;
	protected SelectorEventLoop[]	selectorLoops			= null;
	protected ReentrantLock			runLock				= new ReentrantLock();

	private static final Logger		logger				= LoggerFactory.getLogger(AbstractSelectorLoop.class);

	@Override
	public boolean isWaitForRegist() {
		return isWaitForRegist;
	}

	@Override
	public void setWaitForRegist(boolean isWaitForRegist) {
		this.isWaitForRegist = isWaitForRegist;
	}

	@Override
	public ReentrantLock getIsWaitForRegistLock() {
		return isWaitForRegistLock;
	}

	@Override
	public void setMainSelector(boolean isMainSelector) {
		this.isMainSelector = isMainSelector;
	}

	protected AbstractSelectorLoop(ChannelService service, SelectorEventLoop[] selectorLoops) {

		ChannelContext context = service.getContext();

		this.selectorLoops = selectorLoops;

		this.selectableChannel = service.getSelectableChannel();

		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	protected void cancelSelectionKey(SelectionKey selectionKey, Throwable t) {

		Object attachment = selectionKey.attachment();

		if (attachment == null) {
			return;
		}

		logger.error(t.getMessage() + " channel:" + attachment, t);

		CloseUtil.close((Channel) attachment);

	}

	protected void cancelSelectionKey(SelectionKey selectionKey) {

		Object attachment = selectionKey.attachment();

		if (attachment instanceof Channel) {

			CloseUtil.close((Channel) attachment);
		}
	}

	@Override
	public boolean isMainSelector() {
		return isMainSelector;
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	@Override
	public Selector getSelector() {
		return selector;
	}

	@Override
	protected void doLoop() {

		try {

			selectorLoopStrategy.loop(this);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	private Selector rebuildSelector0() {

		Selector selector;
		try {
			selector = buildSelector(selectableChannel);
		} catch (IOException e) {
			throw new Error(e);
		}

		Selector old = this.selector;

		Set<SelectionKey> sks = old.keys();

		if (sks.size() == 0) {
			logger.debug("sk size 0");
			CloseUtil.close(old);
			return selector;
		}

		for (SelectionKey sk : sks) {

			if (!sk.isValid() || sk.attachment() == null) {
				cancelSelectionKey(sk);
				continue;
			}

			try {
				sk.channel().register(selector, SelectionKey.OP_READ);
			} catch (ClosedChannelException e) {
				cancelSelectionKey(sk, e);
			}
		}

		CloseUtil.close(old);

		return selector;
	}

	@Override
	public void rebuildSelector() {
		this.selector = rebuildSelector0();
	}

	@Override
	public void doStartup() throws IOException {
		this.selector = buildSelector(selectableChannel);
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	@Override
	public void wakeup() {
		selector.wakeup();
		super.wakeup();
	}

	@Override
	public void dispatch(SelectorLoopEvent event) throws RejectedExecutionException {

		if (inEventLoop()) {

			if (!isRunning()) {
				CloseUtil.close(event);
				return;
			}

			getSelectorLoopStrategy().handleEvent(this, event);

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

	private void dispatch0(SelectorLoopEvent event) {

		if (!isRunning()) {
			CloseUtil.close(event);
			return;
		}

		selectorLoopStrategy.fireEvent(event);
	}

	@Override
	public SelectorLoopStrategy getSelectorLoopStrategy() {
		return selectorLoopStrategy;
	}

	@Override
	public SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException {
		return null;
	}

}
