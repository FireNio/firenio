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
package com.firenio.baseio.codec.redis;

import java.io.IOException;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

/**
 * @author wangkai
 *
 */
//FIXME 完善心跳
//FIXME limit
public class RedisCodec extends ProtocolCodec {

    public static final byte    BYTE_ARRAYS            = '*';
    public static final byte    BYTE_BULK_STRINGS      = '$';
    public static final byte    BYTE_ERRORS            = '-';
    public static final byte    BYTE_INTEGERS          = ':';
    public static final byte    BYTE_SIMPLE_STRINGS    = '+';
    public static final String  CMD_PING               = "PING";
    public static final String  CMD_PONG               = "PONG";
    public static final byte[]  CRLF_BYTES             = "\r\n".getBytes();
    public static final char    TYPE_ARRAYS            = '*';
    public static final char    TYPE_BULK_STRINGS      = '$';
    public static final char    TYPE_ERRORS            = '-';

    public static final char    TYPE_INTEGERS          = ':';

    public static final char    TYPE_SIMPLE_STRINGS    = '+';

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        RedisFrame f = (RedisFrame) ch.getAttachment();
        if (f == null) {
            f = new RedisFrame();
        }
        StringBuilder currentLine = f.currentLine;
        RedisNode currentNode = f.currentNode;
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
                                doComplete(ch, f);
                                return f;
                            }
                            currentNode = n;
                        }
                        break;
                    case TYPE_ERRORS:
                        currentNode.setType(TYPE_ERRORS);
                        currentNode.setValue(line.substring(1));
                        doComplete(ch, f);
                        return f;
                    case TYPE_INTEGERS:
                        int intValue = Integer.parseInt(line.substring(1));
                        currentNode.setValue(intValue);
                        currentNode.setType(TYPE_INTEGERS);
                        RedisNode n3 = currentNode.deepNext();
                        if (n3 == null) {
                            doComplete(ch, f);
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
                            doComplete(ch, f);
                            return f;
                        }
                        currentNode = n4;
                        break;
                    default:
                        currentNode.setValue(line);
                        RedisNode n5 = currentNode.deepNext();
                        if (n5 == null) {
                            doComplete(ch, f);
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
        ch.setAttachment(f);
        return null;
    }

    private void doComplete(Channel ch, RedisFrame f) {
        ch.setAttachment(null);
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
    public ByteBuf encode(Channel ch, Frame frame) throws IOException {
        AbstractRedisFrame f = (AbstractRedisFrame) frame;
        ByteBuf buf = f.getBufContent();
        buf.flip();
        if (!buf.hasRemaining()) {
            buf.release();
            throw new IOException("null write buffer");
        }
        return buf;
    }

    @Override
    public String getProtocolId() {
        return "Redis";
    }

    @Override
    public int headerLength() {
        return 0;
    }

    @Override
    public Frame ping(Channel ch) {
        AbstractRedisFrame f = new RedisCmdFrame();
        f.setPing();
        f.writeCommand(RedisCommand.PING.raw);
        return f;
    }

    @Override
    public Frame pong(Channel ch, Frame ping) {
        AbstractRedisFrame f = (AbstractRedisFrame) ping;
        f.setPong();
        f.writeCommand(RedisCommand.PONG.raw);
        return f;
    }

    public enum RedisCommand {

        APPEND, ASKING, AUTH, BGREWRITEAOF, BGSAVE, BITCOUNT, BITOP, BLPOP, BRPOP, BRPOPLPUSH, CLIENT, CLUSTER, CONFIG, DBSIZE, DEBUG, DECR, DECRBY, DEL, DISCARD, DUMP, ECHO, EVAL, EVALSHA, EXEC, EXISTS, EXPIRE, EXPIREAT, FLUSHALL, FLUSHDB, GET, GETBIT, GETRANGE, GETSET, HDEL, HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HLEN, HMGET, HMSET, HSCAN, HSET, HSETNX, HVALS, INCR, INCRBY, INCRBYFLOAT, INFO, KEYS, LASTSAVE, LINDEX, LINSERT, LLEN, LPOP, LPUSH, LPUSHX, LRANGE, LREM, LSET, LTRIM, MGET, MIGRATE, MONITOR, MOVE, MSET, MSETNX, MULTI, OBJECT, PERSIST, PEXPIRE, PEXPIREAT, PING, PONG, PSETEX, PSUBSCRIBE, PTTL, PUBLISH, PUBSUB, PUNSUBSCRIBE, QUIT, RANDOMKEY, RENAME, RENAMENX, RENAMEX, RESTORE, RPOP, RPOPLPUSH, RPUSH, RPUSHX, SADD, SAVE, SCAN, SCARD, SCRIPT, SDIFF, SDIFFSTORE, SELECT, SENTINEL, SET, SETBIT, SETEX, SETNX, SETRANGE, SHUTDOWN, SINTER, SINTERSTORE, SISMEMBER, SLAVEOF, SLOWLOG, SMEMBERS, SMOVE, SORT, SPOP, SRANDMEMBER, SREM, SSCAN, STRLEN, SUBSCRIBE, SUBSTR, SUNION, SUNIONSTORE, SYNC, TIME, TTL, TYPE, UNSUBSCRIBE, UNWATCH, WAIT, WATCH, ZADD, ZCARD, ZCOUNT, ZINCRBY, ZINTERSTORE, ZRANGE, ZRANGEBYSCORE, ZRANK, ZREM, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZREVRANGE, ZREVRANGEBYSCORE, ZREVRANK, ZSCAN, ZSCORE, ZUNIONSTORE;

        public final byte[] raw;

        RedisCommand() {
            raw = name().getBytes();
        }
    }

}
