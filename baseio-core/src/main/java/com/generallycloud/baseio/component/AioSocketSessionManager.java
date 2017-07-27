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

import com.generallycloud.baseio.collection.IntObjectHashMap;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class AioSocketSessionManager extends AbstractSocketSessionManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public AioSocketSessionManager(SocketChannelContext context,
			ExecutorEventLoop selectorEventLoop) {
		super(context);
		this.selectorEventLoop = selectorEventLoop;
	}

	private ExecutorEventLoop selectorEventLoop;

	public void offerSessionMEvent(final SocketSessionManagerEvent event) {

		this.selectorEventLoop.dispatch(new Runnable() {

			@Override
			public void run() {

				IntObjectHashMap<SocketSession> sessions = AioSocketSessionManager.this.sessions;

				if (sessions.size() == 0) {
					return;
				}

				try {
					event.fire(context, sessions);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

}
