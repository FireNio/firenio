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

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
//FIXME 完善心跳
//FIXME limit
public class RedisCodec extends ProtocolCodec {
    
    public static final char   TYPE_SIMPLE_STRINGS = '+';
    public static final char   TYPE_ERRORS         = '-';
    public static final char   TYPE_INTEGERS       = ':';
    public static final char   TYPE_BULK_STRINGS   = '$';
    public static final char   TYPE_ARRAYS         = '*';
    public static final byte   BYTE_SIMPLE_STRINGS = '+';
    public static final byte   BYTE_ERRORS         = '-';
    public static final byte   BYTE_INTEGERS       = ':';
    public static final byte   BYTE_BULK_STRINGS   = '$';
    public static final byte   BYTE_ARRAYS         = '*';
    public static final String CMD_PING            = "PING";
    public static final String CMD_PONG            = "PONG";

    public static final byte[] CRLF_BYTES          = "\r\n".getBytes();

    public enum RedisCommand {

        PING, PONG, SET, GET, QUIT, EXISTS, DEL, TYPE, FLUSHDB, KEYS, RANDOMKEY, RENAME, RENAMENX, RENAMEX, DBSIZE, EXPIRE, EXPIREAT, TTL, SELECT, MOVE, FLUSHALL, GETSET, MGET, SETNX, SETEX, MSET, MSETNX, DECRBY, DECR, INCRBY, INCR, APPEND, SUBSTR, HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, RPOPLPUSH, SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD, SISMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE, SDIFF, SDIFFSTORE, SRANDMEMBER, ZADD, ZRANGE, ZREM, ZINCRBY, ZRANK, ZREVRANK, ZREVRANGE, ZCARD, ZSCORE, MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, BLPOP, BRPOP, AUTH, SUBSCRIBE, PUBLISH, UNSUBSCRIBE, PSUBSCRIBE, PUNSUBSCRIBE, PUBSUB, ZCOUNT, ZRANGEBYSCORE, ZREVRANGEBYSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZUNIONSTORE, ZINTERSTORE, SAVE, BGSAVE, BGREWRITEAOF, LASTSAVE, SHUTDOWN, INFO, MONITOR, SLAVEOF, CONFIG, STRLEN, SYNC, LPUSHX, PERSIST, RPUSHX, ECHO, LINSERT, DEBUG, BRPOPLPUSH, SETBIT, GETBIT, SETRANGE, GETRANGE, EVAL, EVALSHA, SCRIPT, SLOWLOG, OBJECT, BITCOUNT, BITOP, SENTINEL, DUMP, RESTORE, PEXPIRE, PEXPIREAT, PTTL, INCRBYFLOAT, PSETEX, CLIENT, TIME, MIGRATE, HINCRBYFLOAT, SCAN, HSCAN, SSCAN, ZSCAN, WAIT, CLUSTER, ASKING;

        public final byte[] raw;

        RedisCommand() {
            raw = name().getBytes();
        }
    }
    
    private static final String REDIS_DECODE_FRAME_KEY = "_REDIS_DECODE_FRAME_KEY"; 

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        RedisFrameImpl f = (RedisFrameImpl) ch.getAttribute(REDIS_DECODE_FRAME_KEY);
        if (f == null) {
            f = new RedisFrameImpl();
        }
        StringBuilder currentLine = f.currentLine;
        RedisNode     currentNode = f.currentNode;
        for (; src.hasRemaining();) {
            byte b = src.getByte();
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
                                doComplete(ch,f);
                                return f;
                            }
                            currentNode = n;
                        }
                        break;
                    case TYPE_ERRORS:
                        currentNode.setType(TYPE_ERRORS);
                        currentNode.setValue(line.substring(1));
                        doComplete(ch,f);
                        return f;
                    case TYPE_INTEGERS:
                        int intValue = Integer.parseInt(line.substring(1));
                        currentNode.setValue(intValue);
                        currentNode.setType(TYPE_INTEGERS);
                        RedisNode n3 = currentNode.deepNext();
                        if (n3 == null) {
                            doComplete(ch,f);
                            return f;
                        }
                        currentNode = n3;
                        break;
                    case TYPE_SIMPLE_STRINGS:
                        currentNode.setType(TYPE_SIMPLE_STRINGS);
                        String strValue = line.substring(1);
                        currentNode.setValue(strValue);
                        RedisNode n4 = currentNode.deepNext();
                        if (n4 == null) {
                            doComplete(ch,f);
                            return f;
                        }
                        currentNode = n4;
                        break;
                    default:
                        currentNode.setValue(line);
                        RedisNode n5 = currentNode.deepNext();
                        if (n5 == null) {
                            doComplete(ch,f);
                            return f;
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
        ch.setAttribute(REDIS_DECODE_FRAME_KEY, f);
        return null;
    }
    
    private void doComplete(NioSocketChannel ch,RedisFrameImpl f) {
        ch.removeAttribute(REDIS_DECODE_FRAME_KEY);
        //FIXME redis的心跳有些特殊
        //      if (rootNode.getType() == TYPE_SIMPLE_STRINGS) {
        //          
        //          Object value = rootNode.getValue();
        //          
        //          if (CMD_PING.equals(value)) {
        //              setPING();
        //          }else if(CMD_PONG.equals(value)){
        //              setPONG();
        //          }
        //      }
    }

    @Override
    public Frame ping(NioSocketChannel ch) {
        RedisCmdFrame f = new RedisCmdFrame();
        f.setPing();
        f.writeCommand(RedisCommand.PING.raw);
        return f;
    }

    @Override
    public Frame pong(NioSocketChannel ch, Frame ping) {
        RedisCmdFrame f = (RedisCmdFrame) ping;
        f.setPong();
        f.writeCommand(RedisCommand.PONG.raw);
        return f;
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        RedisFrame f = (RedisFrame) frame;
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            throw new IOException("null write buffer");
        }
        ByteBuf buf = UnpooledByteBufAllocator.getHeap().wrap(f.getWriteBuffer(), 0, writeSize);
        return buf.flip();
    }

    @Override
    public String getProtocolId() {
        return "Redis";
    }

}
