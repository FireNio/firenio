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
package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.ByteBuf;

public abstract class LinkableChannelByteBufReader implements ChannelByteBufReader{

	private Linkable<ChannelByteBufReader> next;
	
	@Override
	public Linkable<ChannelByteBufReader> getNext() {
		return next;
	}

	@Override
	public void setNext(Linkable<ChannelByteBufReader> next) {
		this.next = next;
	}

	@Override
	public ChannelByteBufReader getValue() {
		return this;
	}
	
	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}
	
	protected void nextAccept(SocketChannel channel,ByteBuf buffer) throws Exception{
		getNext().getValue().accept(channel, buffer);
	}
	
}
