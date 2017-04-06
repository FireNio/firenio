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
package com.generallycloud.baseio.protocol;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.Session;
import com.generallycloud.baseio.component.SocketChannelContext;


public abstract class AbstractChannelReadFuture extends AbstractReadFuture implements ChannelReadFuture {

	protected AbstractChannelReadFuture(SocketChannelContext context) {
		super(context);
	}

	protected boolean	isHeartbeat;

	protected boolean	isPING;

	protected boolean isSilent;

	@Override
	public ChannelReadFuture flush() {
		flushed = true;
		return this;
	}

	@Override
	public boolean isHeartbeat() {
		return isHeartbeat;
	}

	@Override
	public boolean isPING() {
		return isHeartbeat && isPING;
	}

	@Override
	public boolean isPONG() {
		return isHeartbeat && !isPING;
	}

	@Override
	public ChannelReadFuture setPING() {
		this.isPING = true;
		this.isHeartbeat = true;
		return this;
	}
	
	@Override
	public ChannelReadFuture setPONG() {
		this.isPING = false;
		this.isHeartbeat = true;
		return this;
	}

	@Override
	public boolean isSilent() {
		return isSilent;
	}

	@Override
	public void setSilent(boolean isSilent) {
		this.isSilent = isSilent;
	}

	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}
	
	protected ByteBuf allocate(Session session,int capacity,int maxLimit){
		return session.getByteBufAllocator().allocate(capacity,maxLimit);
	}

}
