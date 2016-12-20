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
package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.ReadFuture;

public interface Http2Frame extends ReadFuture{
	
	public abstract byte getFlags();

	public abstract Http2FrameType getHttp2FrameType();
	
	public abstract int getStreamIdentifier();
	
	public abstract void setStreamIdentifier(int streamIdentifier);
	
}
