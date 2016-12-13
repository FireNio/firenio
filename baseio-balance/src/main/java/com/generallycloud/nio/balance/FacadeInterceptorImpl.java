package com.generallycloud.nio.balance;

public class FacadeInterceptorImpl implements FacadeInterceptor{

	public boolean intercept(BalanceFacadeSocketSession session, BalanceReadFuture future) throws Exception {
		return false;
	}

}
