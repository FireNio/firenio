package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;

public interface FrontInterceptor {

	public abstract boolean intercept(FrontFacadeSocketSession session, BalanceReadFuture future) throws Exception;
}
