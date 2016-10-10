package com.generallycloud.nio.balance;

public interface HashedBalanceReadFuture extends BalanceReadFuture {

	public void setHashCode(int hashCode);

	public int getHashCode();
}
