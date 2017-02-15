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
package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.DatagramSessionManager.DatagramSessionManagerEvent;

public interface DatagramChannelContext extends ChannelContext {
	
	public abstract void setSessionManager(DatagramSessionManager sessionManager) ;
	
	@Override
	public abstract DatagramSessionManager getSessionManager();

	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();

	public abstract void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor);

	public abstract Linkable<DatagramSessionEventListener> getSessionEventListenerLink();
	
	public abstract void addSessionEventListener(DatagramSessionEventListener listener);
	
	public abstract void offerSessionMEvent(DatagramSessionManagerEvent event);
	
	
}
