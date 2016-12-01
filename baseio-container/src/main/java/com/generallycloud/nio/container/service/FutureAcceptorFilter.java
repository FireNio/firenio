package com.generallycloud.nio.container.service;

import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.HotDeploy;
import com.generallycloud.nio.container.Initializeable;
import com.generallycloud.nio.container.InitializeableImpl;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class FutureAcceptorFilter extends InitializeableImpl implements Initializeable, HotDeploy, IoEventHandle {
	
	private int sortIndex;
	
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
	}
	
	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.accept(session, (NamedReadFuture)future);
	}
	
	protected abstract void accept(SocketSession session, NamedReadFuture future) throws Exception;

	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}
	
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		
	}

	public void futureSent(SocketSession session, ReadFuture future) {
		
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
	
}
