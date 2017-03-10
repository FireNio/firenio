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
package com.generallycloud.baseio.codec.protobase.future;

import com.generallycloud.baseio.balance.HashedBalanceReadFuture;
import com.generallycloud.baseio.component.BufferedOutputStream;
import com.generallycloud.baseio.protocol.NamedReadFuture;
import com.generallycloud.baseio.protocol.ParametersReadFuture;

public interface ProtobaseReadFuture extends HashedBalanceReadFuture, NamedReadFuture , ParametersReadFuture{

	public abstract int getTextLength();

	public abstract int getBinaryLength();

	public abstract boolean hasBinary();

	public abstract byte[] getBinary();

	public abstract int getFutureID();
	
	@Override
	public abstract Long getToken();
	
	public abstract void setToken(long token);

	public abstract void setFutureID(int futureID);

	public abstract BufferedOutputStream getWriteBinaryBuffer();

	public abstract void writeBinary(byte b);

	public abstract void writeBinary(byte[] bytes);

	public abstract void writeBinary(byte[] bytes, int offset, int length);
}
