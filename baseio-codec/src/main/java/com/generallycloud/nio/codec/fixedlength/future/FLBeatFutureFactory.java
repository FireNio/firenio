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
package com.generallycloud.nio.codec.fixedlength.future;

import com.generallycloud.nio.component.BeatFutureFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FLBeatFutureFactory implements BeatFutureFactory{

	@Override
	public ReadFuture createPINGPacket(SocketSession session) {
		return new FixedLengthReadFutureImpl(session.getContext()).setPING();
	}

	@Override
	public ReadFuture createPONGPacket(SocketSession session) {
		return new FixedLengthReadFutureImpl(session.getContext()).setPONG();
	}
	
}
