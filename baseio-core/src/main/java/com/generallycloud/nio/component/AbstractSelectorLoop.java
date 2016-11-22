package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class AbstractSelectorLoop implements SelectorLoop {

	private Thread								monitor			= null;
	private boolean							isMainSelector		= false;
	private boolean							isWaitForRegist	= false;
	private boolean							hasTask			= false;
	private int								runTask			= 0;
	private byte[]							isWaitForRegistLock	= new byte[] {};
	private byte[]							runLock			= new byte[] {};

	protected ByteBufAllocator					byteBufAllocator	= null;
	protected BaseContext						context			= null;
	protected ProtocolDecoder					protocolDecoder	= null;
	protected ProtocolEncoder					protocolEncoder	= null;
	protected ProtocolFactory					protocolFactory	= null;
	protected SelectableChannel					selectableChannel	= null;
	protected Selector							selector			= null;
	protected boolean							shutdown			= false;
	protected boolean							working			= false;
	protected BufferedArrayList<SelectorLoopEvent>	events			= new BufferedArrayList<SelectorLoopEvent>();

	private static final Logger					logger			= LoggerFactory
																	.getLogger(AbstractSelectorLoop.class);
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

	protected AbstractSelectorLoop(BaseContext context, SelectableChannel selectableChannel) {

		this.context = context;

		this.selectableChannel = selectableChannel;

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

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

	public abstract void accept(SelectionKey key);

	protected void cancelSelectionKey(SelectionKey sk) {

		Object attachment = sk.attachment();

		if (attachment instanceof Channel) {
			CloseUtil.close((Channel) attachment);
		}
	}

	protected void cancelSelectionKey(SelectionKey selectionKey, Throwable t) {

		cancelSelectionKey(selectionKey);

		logger.error(t.getMessage(), t);
	}
	
	public boolean isMainSelector() {
		return isMainSelector;
	}

	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}

	public BaseContext getContext() {
		return context;
	}

	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

	public Selector getSelector() {
		return selector;
	}

	private void waitForRegist() throws InterruptedException {

		synchronized (isWaitForRegistLock) {
		}
	}

	public void loop() {

		working = true;

		if (shutdown) {
			working = false;
			return;
		}

		try {

			if (isWaitForRegist) {

				waitForRegist();
			}

			Selector selector = this.selector;

			int selected;

			// long last_select = System.currentTimeMillis();
			
			
			if (hasTask) {
				
				if (runTask-- > 0) {
					
					handleEvents(false);

					working = false;
					
					return;
				}
				
				selected = selector.selectNow();
			}else{
				
				selected = selector.select(8);
			}
			
			if (selected < 1) {

				// selectEmpty(last_select);
			}else{
				
				Set<SelectionKey> selectionKeys = selector.selectedKeys();

				for (SelectionKey key : selectionKeys) {

					accept(key);
				}

				selectionKeys.clear();
			}
			
			handleEvents(true);

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
		}
	}

	private void handleEvents(boolean refresh) {

		List<SelectorLoopEvent> eventBuffer = events.getBuffer();

		if (eventBuffer.size() == 0) {
			
			hasTask = false;

			return;
		}

		for (SelectorLoopEvent event : eventBuffer) {

			try {

				if (event.handle(this)) {

					events.offer(event);
				}

			} catch (IOException e) {

				CloseUtil.close(event);

				continue;
			}
		}
		
		hasTask = events.getBufferSize() > 0;
		
		if (hasTask && refresh) {
			runTask = 5;
		}
	}

	private Selector rebuildSelector() {

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

	private void selectEmpty(long last_select) {

		long past = System.currentTimeMillis() - last_select;

		if (past < 1000) {

			if (shutdown || past < 0) {
				working = false;
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);
			this.selector = rebuildSelector();
		}

		working = false;

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

			List<SelectorLoopEvent> eventBuffer = events.getBuffer();

			for (SelectorLoopEvent event : eventBuffer) {

				CloseUtil.close(event);
			}
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

			events.offer(event);
		}
	}

}
