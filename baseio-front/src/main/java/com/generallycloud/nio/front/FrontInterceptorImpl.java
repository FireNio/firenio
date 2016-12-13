package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;

public class FrontInterceptorImpl implements FrontInterceptor{

	// FIXME 按时间单位拦截请求
	public boolean intercept(FrontFacadeSocketSession session, BalanceReadFuture future) throws Exception {
		
		if (future.isReceiveBroadcast()) {
			session.setReceiveBroadcast(true);
			return true;
		}

		return false;
	}

}
