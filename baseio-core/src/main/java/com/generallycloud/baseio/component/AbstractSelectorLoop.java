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
package com.generallycloud.baseio.component;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class AbstractSelectorLoop extends AbstractEventLoop implements SelectorEventLoop {

    private static final Logger logger           = LoggerFactory
            .getLogger(AbstractSelectorLoop.class);

    private int                 coreIndex;

    private ByteBufAllocator    byteBufAllocator = null;

    private boolean             mainEventLoop    = true;

    @Override
    public boolean isMainEventLoop() {
        return mainEventLoop;
    }

    protected AbstractSelectorLoop(ChannelContext context, int coreIndex) {
        this.setCoreIndex(coreIndex);
        this.byteBufAllocator = context.getByteBufAllocatorManager().getNextBufAllocator();
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return byteBufAllocator;
    }

    protected void cancelSelectionKey(Channel channel, Throwable t) {
        logger.error(t.getMessage() + " channel:" + channel, t);
        CloseUtil.close(channel);
    }

    @Override
    public void doStartup() throws IOException {
        rebuildSelector();
    }

    @Override
    public int getCoreIndex() {
        return coreIndex;
    }

    private void setCoreIndex(int coreIndex) {
        this.coreIndex = coreIndex;
        this.mainEventLoop = coreIndex == 0;
    }

}
