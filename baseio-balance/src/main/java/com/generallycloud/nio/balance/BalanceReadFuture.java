/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.balance;

import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceReadFuture extends ReadFuture {

	public static int	BROADCAST	= 1;

	public static int	PUSH		= 0;

	public abstract Object getFutureID();

	public abstract void setFutureID(Object futureID);

	public abstract Integer getSessionID();

	public abstract void setSessionID(Integer sessionID);
	
	public abstract boolean isBroadcast();

	public abstract void setBroadcast(boolean broadcast);

	public abstract BalanceReadFuture translate();

}
