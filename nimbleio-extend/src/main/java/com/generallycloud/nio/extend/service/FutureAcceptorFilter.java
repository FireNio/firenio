package com.generallycloud.nio.extend.service;

import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.HotDeploy;
import com.generallycloud.nio.extend.Initializeable;
import com.generallycloud.nio.extend.InitializeableImpl;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class FutureAcceptorFilter extends InitializeableImpl implements Initializeable, HotDeploy, IOEventHandle {
	
	private int sortIndex;
	
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
	}
	
	public void accept(Session session, ReadFuture future) throws Exception {
		this.accept(session, (NamedReadFuture)future);
	}
	
	protected abstract void accept(Session session, NamedReadFuture future) throws Exception;

	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		
	}

	public void futureSent(Session session, ReadFuture future) {
		
	}

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}
	
}
