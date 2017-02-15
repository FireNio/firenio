/*
 * Copyright 2015-2017 GenerallyCloud.com
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

import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractBalanceReadFuture extends AbstractChannelReadFuture implements BalanceReadFuture {

	protected boolean	isBroadcast;

//	protected Integer	clientSessionID;
//	
//	protected Integer	frontSessionID;
	
	protected Integer sessionID = 0;

	protected AbstractBalanceReadFuture(SocketChannelContext context) {
		super(context);
	}

//	@Override
//	public Integer getClientSessionID() {
//		if (clientSessionID == null) {
//			clientSessionID = 0;
//		}
//		return clientSessionID;
//	}
//
//	@Override
//	public void setClientSessionID(Integer sessionID) {
//		this.clientSessionID = sessionID;
//	}
//
//	@Override
//	public Integer getFrontSessionID() {
//		if (frontSessionID == null) {
//			frontSessionID = 0;
//		}
//		return frontSessionID;
//	}
//
//	@Override
//	public void setFrontSessionID(Integer sessionID) {
//		this.frontSessionID = sessionID;
//	}
	
	@Override
	public boolean isBroadcast() {
		return isBroadcast;
	}
	
	@Override
	public Integer getSessionID() {
		return sessionID;
	}

	@Override
	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public void setBroadcast(boolean isBroadcast) {
		this.isBroadcast = isBroadcast;
	}
	
}
