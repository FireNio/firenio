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

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.AbstractEventLoop;

public abstract class AbstractSelectorLoop extends AbstractEventLoop implements SelectorEventLoop {

	private static final Logger					logger				= LoggerFactory
			.getLogger(AbstractSelectorLoop.class);
	protected ByteBufAllocator					byteBufAllocator		= null;

	protected AbstractSelectorLoop(ChannelContext context) {
		this.byteBufAllocator = context.getMcByteBufAllocator().getNextBufAllocator();
	}

	@Override
	public ByteBufAllocator getByteBufAllocator() {
		return byteBufAllocator;
	}
	
	protected void cancelSelectionKey(SocketChannel channel, Throwable t) {
		
		logger.error(t.getMessage() + " channel:" + channel, t);
		
		CloseUtil.close(channel);
	}
	
	@Override
	public void doStartup() throws IOException {
		rebuildSelector();
	}

}
