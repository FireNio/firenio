package com.generallycloud.nio.extend.service;

import com.generallycloud.nio.component.Configuration;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.HotDeploy;
import com.generallycloud.nio.extend.Initializeable;
import com.generallycloud.nio.extend.InitializeableImpl;

public abstract class FutureAcceptorFilter extends InitializeableImpl implements Initializeable, HotDeploy, IOEventHandle {
	
	private int sortIndex;
	
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
	}
	
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
