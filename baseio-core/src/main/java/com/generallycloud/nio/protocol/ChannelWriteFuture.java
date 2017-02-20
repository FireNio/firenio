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
package com.generallycloud.nio.protocol;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.component.SocketChannel;

public interface ChannelWriteFuture extends WriteFuture, Linkable<ChannelWriteFuture> {

	public abstract void write(SocketChannel channel) throws IOException;

	public abstract boolean isCompleted();
	
	public abstract ByteBuf getByteBuf();
	
	public abstract ChannelWriteFuture duplicate();
	
	public abstract ChannelWriteFuture duplicate(ReadFuture future);

	public abstract void onException(SocketSession session, Exception e);

	public abstract void onSuccess(SocketSession session);
	
	public abstract int getBinaryLength();

	public abstract void wrapSSL(SocketChannel channel, SslHandler handler) throws SSLException, IOException;
}
