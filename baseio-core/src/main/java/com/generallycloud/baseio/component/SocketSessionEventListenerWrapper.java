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

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;

public class SocketSessionEventListenerWrapper extends AbstractLinkable<SocketSessionEventListener> implements SocketSessionEventListener {

	private SocketSessionEventListenerWrapper next;
	
	private Logger logger;
	
	public SocketSessionEventListenerWrapper(SocketSessionEventListener value) {
		super(value);
		logger = LoggerFactory.getLogger(value.getClass());
	}

	@Override
	public void sessionOpened(SocketSession session) throws Exception {
		
		try {
			getValue().sessionOpened(session);
		} catch (Exception e) {
			logger.errorDebug(e);
		}

		SocketSessionEventListenerWrapper listener = getNext();

		if (listener == null) {
			return;
		}

		listener.sessionOpened(session);
	}

	@Override
	public void sessionClosed(SocketSession session) {
		
		try {
			getValue().sessionClosed(session);
		} catch (Exception e) {
			logger.errorDebug(e);
		}

		SocketSessionEventListenerWrapper listener = getNext();

		if (listener == null) {
			return;
		}

		listener.sessionClosed(session);
	}

	@Override
	public SocketSessionEventListenerWrapper getNext() {
		return next;
	}

	@Override
	public void setNext(Linkable<SocketSessionEventListener> next) {
		this.next = (SocketSessionEventListenerWrapper) next;
	}

}
