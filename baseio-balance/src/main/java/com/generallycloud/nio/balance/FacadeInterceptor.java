package com.generallycloud.nio.balance;

public interface FacadeInterceptor {

	public abstract boolean intercept(BalanceFacadeSocketSession session, BalanceReadFuture future) throws Exception;
}
