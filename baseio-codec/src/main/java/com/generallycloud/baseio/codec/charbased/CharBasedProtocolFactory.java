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
package com.generallycloud.baseio.codec.charbased;

import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public class CharBasedProtocolFactory implements ProtocolFactory {

    private int limit;
    
    private byte splitor;

    public CharBasedProtocolFactory() {
        this(1024 * 8,(byte) '\n');
    }
    
    public CharBasedProtocolFactory(byte splitor) {
        this(1024 * 8,splitor);
    }

    public CharBasedProtocolFactory(int limit,byte splitor) {
        this.limit = limit;
        this.splitor = splitor;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return new CharBasedProtocolDecoder(limit,splitor);
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return new CharBasedProtocolEncoder(splitor);
    }

    @Override
    public String getProtocolId() {
        return "LineBased";
    }

}
