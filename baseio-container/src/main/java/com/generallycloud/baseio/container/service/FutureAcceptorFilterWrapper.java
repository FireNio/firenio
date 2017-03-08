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
package com.generallycloud.baseio.container.service;

import com.generallycloud.baseio.Linkable;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.protocol.NamedReadFuture;
import com.generallycloud.baseio.protocol.ReadFuture;

public class FutureAcceptorFilterWrapper extends FutureAcceptorFilter implements Linkable<FutureAcceptorFilter> {

	private FutureAcceptorFilter			filter;
	private Linkable<FutureAcceptorFilter>	nextFilter;

	public FutureAcceptorFilterWrapper(ApplicationContext context, FutureAcceptorFilter filter, Configuration config) {
		this.filter = filter;
		this.setConfig(config);
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {
	
		this.filter.accept(session, future);
		
		if (future.flushed()) {

			return;
		}
		
		nextAccept(session, future);
	}
	
	private void nextAccept(SocketSession session, ReadFuture future) throws Exception{
		
		Linkable<FutureAcceptorFilter> next = getNext();
		
		if (next == null) {
			return;
		}
		
		next.getValue().accept(session, future);
	}

	@Override
	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {
		this.filter.accept(session, future);
	}

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		this.filter.exceptionCaught(session, future, cause, state);
	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {
		this.filter.futureSent(session, future);
	}

	@Override
	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);
	}

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.filter.initialize(context, config);
	}

	@Override
	public String toString() {
		return "Warpper(" + this.filter.toString() + ")";
	}

	@Override
	public Linkable<FutureAcceptorFilter> getNext() {
		return nextFilter;
	}

	@Override
	public void setNext(Linkable<FutureAcceptorFilter> next) {
		this.nextFilter = next;
	}

	@Override
	public FutureAcceptorFilter getValue() {
		return this;
	}

}
