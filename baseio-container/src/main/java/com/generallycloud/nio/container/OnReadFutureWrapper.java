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
package com.generallycloud.nio.container;

import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.protocol.ReadFuture;

public class OnReadFutureWrapper implements OnReadFuture {

	private OnReadFuture				listener	= null;

	private ListQueue<WaiterOnReadFuture>	waiters	= new ListQueueABQ<WaiterOnReadFuture>(1024 * 8);

	@Override
	public void onResponse(final SocketSession session, final ReadFuture future) {

		WaiterOnReadFuture waiter = waiters.poll();

		if (waiter != null) {

			waiter.onResponse(session, future);

			return;
		}

		if (listener == null) {
			return;
		}

		listener.onResponse(session, future);
	}

	public void listen(WaiterOnReadFuture onReadFuture) {
		this.waiters.offer(onReadFuture);
	}

	public OnReadFuture getListener() {
		return listener;
	}

	public void setListener(OnReadFuture listener) {
		this.listener = listener;
	}
}
