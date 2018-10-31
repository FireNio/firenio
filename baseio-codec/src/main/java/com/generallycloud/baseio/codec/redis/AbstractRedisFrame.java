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
package com.generallycloud.baseio.codec.redis;

import com.generallycloud.baseio.protocol.AbstractFrame;

public abstract class AbstractRedisFrame extends AbstractFrame {

    public void writeCommand(byte[] command, byte[]... args) {

        this.write(RedisCodec.BYTE_ARRAYS);
        this.write(String.valueOf(args.length + 1).getBytes());
        this.write(RedisCodec.CRLF_BYTES);
        this.write(RedisCodec.BYTE_BULK_STRINGS);
        this.write(String.valueOf(command.length).getBytes());
        this.write(RedisCodec.CRLF_BYTES);
        this.write(command);
        this.write(RedisCodec.CRLF_BYTES);

        for (byte[] arg : args) {
            this.write(RedisCodec.BYTE_BULK_STRINGS);
            this.write(String.valueOf(arg.length).getBytes());
            this.write(RedisCodec.CRLF_BYTES);
            this.write(arg);
            this.write(RedisCodec.CRLF_BYTES);
        }
    }

}
