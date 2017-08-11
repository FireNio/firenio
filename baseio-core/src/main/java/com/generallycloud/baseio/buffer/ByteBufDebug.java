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
package com.generallycloud.baseio.buffer;

import java.util.Hashtable;

import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class ByteBufDebug {
	
	private static ByteBufDebug debug = new ByteBufDebug();
	
	private static boolean enableDebug = true;
	
	public static ByteBufDebug get(){
		return debug;
	}
	
	private Hashtable<ByteBuf,Object> bufs = new Hashtable<>();
	
	private Hashtable<ByteBuf,Object> errorBufs = new Hashtable<>();
	
	public void put(ByteBuf buf){
		if (!enableDebug) {
			return;
		}
		bufs.put(buf, new Exception().getStackTrace());
	}
	
	public void remove(ByteBuf buf){
		if (!enableDebug) {
			return;
		}
		Object b = bufs.remove(buf);
		if (b == null) {
			DebugUtil.info("test222");
		}
	}
	
	public void putErrorByteBuf(ByteBuf buf){
		errorBufs.put(buf, new Exception().getStackTrace());
	}
	
	public void gcErrors(){
		for(ByteBuf buf : errorBufs.keySet()){
			PooledHeapByteBuf b = (PooledHeapByteBuf)buf;
			if(b.referenceCount == 0){
				errorBufs.remove(b);
			}
		}
	}
	
	/**
	 * @return the bufs
	 */
	public Hashtable<ByteBuf,Object> getBufs() {
		return bufs;
	}
	
	/**
	 * @return the errorBufs
	 */
	public Hashtable<ByteBuf,Object> getErrorBufs() {
		return errorBufs;
	}
	
}
