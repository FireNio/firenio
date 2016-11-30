package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class AbstractSelectorLoop extends AbstractEventLoopThread implements SelectorLoop {

	private boolean				isMainSelector			= false;
	private boolean				isWaitForRegist		= false;
	private byte[]				isWaitForRegistLock		= new byte[] {};

	protected byte[]				runLock				= new byte[] {};
	protected ByteBufAllocator		byteBufAllocator		= null;
	protected SelectableChannel		selectableChannel		= null;
	protected Selector				selector				= null;
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

	public void rebuildSelector() {
		this.selector = rebuildSelector0();
	}

	public void doStartup() throws IOException {
		this.selector = buildSelector(selectableChannel);
	}
	
	// FIXME 会不会出现这种情况，数据已经接收到本地，但是还没有被EventLoop处理完
	// 执行stop的时候如果确保不会再有数据进来
	protected void wakeupThread(){
		
		super.wakeupThread();
		
		this.selector.wakeup();
	}
	
	public void wakeup() {
		selector.wakeup();
	}

	public void fireEvent(SelectorLoopEvent event) {

		synchronized (runLock) {

			if (!isRunning()) {
				CloseUtil.close(event);
				return;
			}

			selectorLoopStrategy.fireEvent(event);
		}
	}

	public SelectorLoopStrategy getSelectorLoopStrategy() {
		return selectorLoopStrategy;
	}
	
	public SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException {
		return null;
	}
	
}
