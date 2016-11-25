package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;

public abstract class AbstractSelectorLoop implements SelectorLoop {

	private Thread					monitor				= null;
	private boolean				isMainSelector			= false;
	private boolean				isWaitForRegist		= false;
	private byte[]				isWaitForRegistLock		= new byte[] {};
	private byte[]				runLock				= new byte[] {};

	protected ByteBufAllocator		byteBufAllocator		= null;
	protected SelectableChannel		selectableChannel		= null;
	protected Selector				selector				= null;
	protected boolean				shutdown				= false;
	protected boolean				working				= false;
	protected SelectorLoopStrategy	selectorLoopStrategy	= null;
	protected SelectorLoop[]		selectorLoops			= null;

	private static final Logger		logger				= LoggerFactory.getLogger(AbstractSelectorLoop.class);

	public boolean isWaitForRegist() {
		return isWaitForRegist;
	}

	public void setWaitForRegist(boolean isWaitForRegist) {
		this.isWaitForRegist = isWaitForRegist;
	}

	public byte[] getIsWaitForRegistLock() {
		return isWaitForRegistLock;
	}

	public void setMainSelector(boolean isMainSelector) {
		this.isMainSelector = isMainSelector;
	}

	protected AbstractSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {

		ChannelContext context = service.getContext();
		
		this.selectorLoops = selectorLoops;

		this.selectableChannel = service.getSelectableChannel();

		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	public Thread getMonitor() {
		return monitor;
	}

	public void setMonitor(Thread monitor) {
		if (this.monitor == null) {
			this.monitor = monitor;
		}
	}

	protected void cancelSelectionKey(SelectionKey selectionKey, Throwable t) {

		Object attachment = selectionKey.attachment();

		if (attachment instanceof Channel) {
			
			logger.error(t.getMessage()+" channel:" + attachment, t);
			
			CloseUtil.close((Channel) attachment);
			
		} else {
			
			logger.error(t.getMessage(), t);
		}
	}
	
	protected void cancelSelectionKey(SelectionKey selectionKey) {

		Object attachment = selectionKey.attachment();

		if (attachment instanceof Channel) {
			
			CloseUtil.close((Channel) attachment);
		}
	}

	public boolean isMainSelector() {
		return isMainSelector;
	}

	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	public void loop() {

		working = true;

		if (shutdown) {
			working = false;
			return;
		}

		try {

			selectorLoopStrategy.loop(this);

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
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

	public void rebuildSelector() {
		this.selector = rebuildSelector0();
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void startup() throws IOException {

		this.selector = buildSelector(selectableChannel);
	}

	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	public void stop() {

		this.shutdown = true;

		this.selector.wakeup();

		for (; working;) {

			ThreadUtil.sleep(8);
		}

		synchronized (runLock) {
			selectorLoopStrategy.stop();
		}

		try {
			this.selector.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void wakeup() {
		selector.wakeup();
	}

	public void fireEvent(SelectorLoopEvent event) {

		synchronized (runLock) {

			if (shutdown) {
				CloseUtil.close(event);
				return;
			}

			selectorLoopStrategy.fireEvent(event);
		}
	}

	public SelectorLoopStrategy getSelectorLoopStrategy() {
		return selectorLoopStrategy;
	}

}
