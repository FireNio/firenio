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
package com.generallycloud.nio.connector;

import java.io.Closeable;
import java.io.IOException;

import com.generallycloud.nio.component.Connectable;
import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;

public interface ChannelConnector extends ChannelService, Connectable, Closeable {

	public abstract Session getSession();
	
	public abstract boolean isConnected();
	
	public abstract long getTimeout() ;

	public abstract void setTimeout(long timeout) ;
	
	public abstract void physicalClose() throws IOException;
	
	public abstract Waiter<IOException> asynchronousClose();
}
