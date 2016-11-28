package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;

public abstract class AbstractSelectorLoopStrategy implements SelectorLoopStrategy {

	private Logger								logger	= LoggerFactory
															.getLogger(AbstractSelectorLoopStrategy.class);

	protected boolean							hasTask	= false;
	protected int								runTask	= 0;
	protected SelectorLoop						selectorLoop;
	protected AtomicBoolean						selecting = new AtomicBoolean();
	protected BufferedArrayList<SelectorLoopEvent>	events	= new BufferedArrayList<SelectorLoopEvent>();
	
	protected AbstractSelectorLoopStrategy(SelectorLoop selectorLoop) {
		this.selectorLoop = selectorLoop;
	}

	protected void selectEmpty(SelectorLoop looper,long last_select) {

		long past = System.currentTimeMillis() - last_select;

		if (past < 1000) {

			if (looper.isShutdown() || past < 0) {
				return;
			}

			// JDK bug fired ?
			IOException e = new IOException("JDK bug fired ?");
			logger.error(e.getMessage(), e);
			logger.debug("last={},past={}", last_select, past);
			
			looper.rebuildSelector();
		}
	}

	protected void handleEvents(SelectorLoop looper, boolean refresh) {

		List<SelectorLoopEvent> eventBuffer = events.getBuffer();

		if (eventBuffer.size() == 0) {

			hasTask = false;

			return;
		}

		for (SelectorLoopEvent event : eventBuffer) {

			try {

				if (event.handle(looper)) {

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

	public void stop() {

		List<SelectorLoopEvent> eventBuffer = events.getBuffer();

		for (SelectorLoopEvent event : eventBuffer) {

			CloseUtil.close(event);
		}
	}

	public void fireEvent(SelectorLoopEvent event) {
		
		events.offer(event);
		
		if (events.getBufferSize() == 1) {
			
			if (selecting.compareAndSet(false, true)) {
				
				selectorLoop.wakeup();
				
				selecting.set(false);
			}
		}
	}
	
	public void regist(java.nio.channels.SocketChannel channel,SelectorLoop selectorLoop) throws IOException{
		
		SelectionKey sk = channel.register(selectorLoop.getSelector(), SelectionKey.OP_READ);
		
		// 绑定SocketChannel到SelectionKey
		SocketChannel socketChannel = selectorLoop.buildSocketChannel(sk);

		// fire session open event
		socketChannel.getSession().fireOpend();
		// logger.debug("__________________chanel____gen____{}", channel);
	}
	
}
