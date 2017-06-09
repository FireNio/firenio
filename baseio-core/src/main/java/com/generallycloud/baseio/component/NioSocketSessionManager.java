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
import java.util.Map;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;

public class NioSocketSessionManager extends AbstractSocketSessionManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public NioSocketSessionManager(SocketChannelContext context) {
		super(context);
	}

	public void initSocketSelectorEventLoop(SocketSelectorEventLoop selectorEventLoop) {
		this.selectorEventLoop = selectorEventLoop;
	}

	private SocketSelectorEventLoop selectorEventLoop;

	public void offerSessionMEvent(final SocketSessionManagerEvent event) {

		this.selectorEventLoop.dispatch(new SelectorLoopEventAdapter() {

			@Override
			public void fireEvent(SocketSelectorEventLoop selectLoop) throws IOException {

				Map<Integer, SocketSession> map = iteratorSessions.takeSnapshot();

				if (map.size() == 0) {
					return;
				}

				try {
					event.fire(context, map);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

}
