/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.codec.http11;

import com.firenio.baseio.buffer.ByteBuf;

/**
 * @author wangkai
 *
 */
public class HttpAttachment {

    private HttpFrame uncompleteFrame;
    
    private String websocketFrameName;
    
    private ByteBuf lastWriteBuf = ByteBuf.empty();
    
    public ByteBuf getLastWriteBuf() {
        return lastWriteBuf;
    }

    public void setLastWriteBuf(ByteBuf lastWriteBuf) {
        this.lastWriteBuf = lastWriteBuf;
    }

    public String getWebsocketFrameName() {
        return websocketFrameName;
    }

    public void setWebsocketFrameName(String websocketFrameName) {
        this.websocketFrameName = websocketFrameName;
    }

    public HttpFrame getUncompleteFrame() {
        return uncompleteFrame;
    }

    public void setUncompleteFrame(HttpFrame uncompleteFrame) {
        this.uncompleteFrame = uncompleteFrame;
    }
    
}
