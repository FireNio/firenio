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

import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.SocketChannelContext;

public interface ReadFuture extends Future {
	
	public abstract IoEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IoEventHandle ioEventHandle);
	
	public abstract SocketChannelContext getContext();
	
	public abstract boolean flushed();
	
	public abstract String getReadText();
	
	public abstract String getWriteText();
	
	public abstract StringBuilder getWriteTextBuffer();
	
	public abstract void write(String text);
	
	public abstract void write(char c);
	
	public abstract void write(boolean b);
	
	public abstract void write(int i);
	
	public abstract void write(long l);
	
	public abstract void write(double d);
	
}
