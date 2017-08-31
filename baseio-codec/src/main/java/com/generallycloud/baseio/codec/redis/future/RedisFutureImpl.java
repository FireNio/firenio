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
package com.generallycloud.baseio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketChannel;

//FIXME 完善心跳
//FIXME limit
public class RedisFutureImpl extends AbstractRedisFuture {

    private StringBuilder currentLine = new StringBuilder();

    private RedisNode     rootNode    = new RedisNode();

    private RedisNode     currentNode = rootNode;

    private boolean       complete    = false;

    public RedisFutureImpl(SocketChannelContext context) {
        super(context);
        //		this.currentLine 	= new StringBuilder();
        //		this.rootNode		= new RedisNode(0);
        //		this.currentNode	= rootNode;
        //		this.complete		= false;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (complete) {
            return true;
        }

        for (; buffer.hasRemaining();) {

            byte b = buffer.getByte();

            if (b == '\n') {

                String line = currentLine.toString();
                currentLine.setLength(0);

                switch (line.charAt(0)) {
                    case TYPE_ARRAYS:

                        int size = Integer.parseInt(line.substring(1));

                        currentNode.createChildren(size);

                        currentNode.setType(TYPE_ARRAYS);

                        currentNode = currentNode.getChildren()[0];

                        break;
                    case TYPE_BULK_STRINGS:

                        currentNode.setType(TYPE_BULK_STRINGS);

                        int length = Integer.parseInt(line.substring(1));

                        if (length == -1) {

                            RedisNode n = currentNode.deepNext();

                            if (n == null) {

                                doComplete();

                                return true;
                            }

                            currentNode = n;
                        }

                        break;
                    case TYPE_ERRORS:

                        currentNode.setType(TYPE_ERRORS);

                        currentNode.setValue(line.substring(1));

                        doComplete();

                        return true;
                    case TYPE_INTEGERS:

                        int intValue = Integer.parseInt(line.substring(1));

                        currentNode.setValue(intValue);

                        currentNode.setType(TYPE_INTEGERS);

                        RedisNode n3 = currentNode.deepNext();

                        if (n3 == null) {

                            doComplete();

                            return true;
                        }

                        currentNode = n3;

                        break;
                    case TYPE_SIMPLE_STRINGS:

                        currentNode.setType(TYPE_SIMPLE_STRINGS);

                        String strValue = line.substring(1);

                        currentNode.setValue(strValue);

                        RedisNode n4 = currentNode.deepNext();

                        if (n4 == null) {

                            doComplete();

                            return true;
                        }

                        currentNode = n4;

                        break;
                    default:

                        currentNode.setValue(line);

                        RedisNode n5 = currentNode.deepNext();

                        if (n5 == null) {

                            doComplete();

                            return true;
                        }

                        currentNode = n5;

                        break;
                }

            } else if (b == '\r') {
                continue;
            } else {
                currentLine.append((char) b);
            }
        }

        return complete;
    }

    private void doComplete() {

        complete = true;

        //FIXME redis的心跳有些特殊
        //		if (rootNode.getType() == TYPE_SIMPLE_STRINGS) {
        //			
        //			Object value = rootNode.getValue();
        //			
        //			if (CMD_PING.equals(value)) {
        //				setPING();
        //			}else if(CMD_PONG.equals(value)){
        //				setPONG();
        //			}
        //		}

    }

    @Override
    public RedisNode getRedisNode() {
        return rootNode;
    }

}
