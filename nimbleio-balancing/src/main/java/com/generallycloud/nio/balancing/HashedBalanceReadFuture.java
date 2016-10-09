package com.generallycloud.nio.balancing;

public interface HashedBalanceReadFuture extends BalanceReadFuture {

	public void setHashCode(int hashCode);

	public int getHashCode();
}
