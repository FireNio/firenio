/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;

/**
 * @author wangkai
 *
 */
public abstract class NioSocketSelector implements SocketSelector {

	private static final Logger		logger			= LoggerFactory.getLogger(NioSocketSelector.class);

	private Selector				selector			= null;

	//FIXME 4096 这个数值是否太小
	private List<NioSocketChannel>	selectedChannels	= new ArrayList<>(4096);

	protected SocketSelectorEventLoop	selectorEventLoop;

	protected SelectableChannel		selectableChannel;

	public NioSocketSelector(SocketSelectorEventLoop selectorEventLoop, Selector selector,
			SelectableChannel selectableChannel) {
		this.selectorEventLoop = selectorEventLoop;
		this.selector = selector;
		this.selectableChannel = selectableChannel;
	}

	@Override
	public int selectNow() throws IOException {
		return selector.selectNow();
	}

	@Override
	public int select(long timeout) throws IOException {
		return selector.select(timeout);
	}

	@Override
	public List<NioSocketChannel> selectedChannels() throws IOException {

		Set<SelectionKey> sks = selector.selectedKeys();

		for (SelectionKey k : sks) {
			
			if (!k.isValid()) {
				continue;
			}

			NioSocketChannel channel = (NioSocketChannel) k.attachment();

			if (channel == null) {
				// channel为空说明该链接未打开
				initSocketChannel(k);
				
				continue;
			}

			selectedChannels.add(channel);
		}

		sks.clear();

		return selectedChannels;
	}

	private void initSocketChannel(SelectionKey k) {
		try {
			buildChannel(k);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public java.nio.channels.Selector getSelector() {
		return selector;
	}

	protected abstract void buildChannel(SelectionKey k) throws IOException;

	@Override
	public void clearSelectedChannels() {
		selectedChannels.clear();
	}

	@Override
	public void close() throws IOException {
		selector.close();
	}

	@Override
	public void wakeup() {
		selector.wakeup();
	}

	protected NioSocketChannel newChannel(SelectionKey selectionKey,SocketSelectorEventLoop selectorLoop) throws SocketException {

		NioSocketChannel channel = (NioSocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(selectorLoop, selectionKey);

		selectionKey.attach(channel);

		return channel;
	}

}
