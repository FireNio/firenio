package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;

public class FrontInterceptorImpl implements FrontInterceptor {

	private int interceptorLimit;

	public FrontInterceptorImpl(int interceptorLimit) {
		this.interceptorLimit = interceptorLimit;
	}

	@Override
	public boolean intercept(FrontFacadeSocketSession session, BalanceReadFuture future) throws Exception {
		return session.overfulfil(interceptorLimit);
	}

}
