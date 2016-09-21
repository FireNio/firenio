package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;

public abstract class AbstractSelectorLoop implements SelectorLoop {

	private Logger		logger	= LoggerFactory.getLogger(AbstractSelectorLoop.class);
	private boolean	working	= false;
	private boolean	shutdown	= false;
	protected Selector	selector;

	public void loop() {

		try {
			working = true;
			
			if (shutdown) {
				working = false;
				return;
			}
			
			Selector selector = this.selector;

			int selected = selector.select(64);

			if (selected < 1) {

				working = false;

				return;
			}

			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			Iterator<SelectionKey> iterator = selectionKeys.iterator();

			for (; iterator.hasNext();) {

				SelectionKey selectionKey = iterator.next();

				iterator.remove();

				accept(selectionKey);
			}

			working = false;

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			working = false;
		}
	}

	public void stop() {
		
		this.shutdown = true;

		this.selector.wakeup();

		for (; working;) {

			ThreadUtil.sleep(8);
		}

		try {
			this.selector.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
