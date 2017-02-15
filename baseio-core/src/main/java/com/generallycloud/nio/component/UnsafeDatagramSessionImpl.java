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
package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class UnsafeDatagramSessionImpl extends DatagramSessionImpl implements UnsafeDatagramSession{

	private static final Logger logger = LoggerFactory.getLogger(UnsafeDatagramSessionImpl.class);
	
	public UnsafeDatagramSessionImpl(DatagramChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	@Override
	public DatagramChannel getDatagramChannel() {
		return channel;
	}

	@Override
	public void fireOpend() {
		
		Linkable<DatagramSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionOpened(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
		
	}

	@Override
	public void fireClosed() {
		
		Linkable<DatagramSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionClosed(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
		
	}

	@Override
	public void physicalClose() {
		
		fireClosed();
	}
	
}
