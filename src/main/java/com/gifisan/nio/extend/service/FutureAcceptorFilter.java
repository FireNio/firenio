package com.gifisan.nio.extend.service;

import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.HotDeploy;
import com.gifisan.nio.extend.Initializeable;
import com.gifisan.nio.extend.InitializeableImpl;
import com.gifisan.nio.extend.configuration.Configuration;

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
