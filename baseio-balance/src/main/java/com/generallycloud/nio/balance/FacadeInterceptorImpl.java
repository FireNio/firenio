package com.generallycloud.nio.balance;

public class FacadeInterceptorImpl implements FacadeInterceptor{

	// FIXME 是否需要设置取消接收广播
	public boolean intercept(BalanceFacadeSocketSession session, BalanceReadFuture future) throws Exception {
		
		if (future.isReceiveBroadcast()) {
			session.setReceiveBroadcast(true);
			return true;
		}

		return false;
	}

}
