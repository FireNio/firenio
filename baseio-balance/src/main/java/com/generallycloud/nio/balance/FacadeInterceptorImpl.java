package com.generallycloud.nio.balance;

public class FacadeInterceptorImpl implements FacadeInterceptor{
	
	private int interceptorLimit;

	public FacadeInterceptorImpl(int interceptorLimit) {
		this.interceptorLimit = interceptorLimit;
	}

	@Override
	public boolean intercept(BalanceFacadeSocketSession session, BalanceReadFuture future) throws Exception {
		
		return session.overfulfil(interceptorLimit);
	}

}
