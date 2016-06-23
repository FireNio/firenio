package com.gifisan.nio.server.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.component.ReadFutureAcceptor;

public interface FutureAcceptorServiceLoader extends LifeCycle, HotDeploy {

	public abstract ReadFutureAcceptor getFutureAcceptor(String serviceName);

}