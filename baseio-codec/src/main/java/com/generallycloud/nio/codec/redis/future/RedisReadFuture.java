package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.protocol.ReadFuture;

public interface RedisReadFuture extends ReadFuture {

	public static final char		TYPE_SIMPLE_STRINGS	= '+';
	public static final char		TYPE_ERRORS			= '-';
	public static final char		TYPE_INTEGERS			= ':';
	public static final char		TYPE_BULK_STRINGS		= '$';
	public static final char		TYPE_ARRAYS			= '*';

	public static final byte		BYTE_SIMPLE_STRINGS	= '+';
	public static final byte		BYTE_ERRORS			= '-';
	public static final byte		BYTE_INTEGERS			= ':';
	public static final byte		BYTE_BULK_STRINGS		= '$';
	public static final byte		BYTE_ARRAYS			= '*';

	public static final String		CMD_PING				= "PING";
	public static final String		CMD_PONG				= "PONG";

	public static byte[]			CRLF_BYTES			= "\r\n".getBytes();

	public abstract RedisNode getRedisNode();

	public enum RedisCommand {

		PING, PONG, SET, GET, QUIT, EXISTS, DEL, TYPE, FLUSHDB, KEYS, RANDOMKEY, RENAME, RENAMENX, RENAMEX, DBSIZE, EXPIRE, EXPIREAT, TTL, SELECT, MOVE, FLUSHALL, GETSET, MGET, SETNX, SETEX, MSET, MSETNX, DECRBY, DECR, INCRBY, INCR, APPEND, SUBSTR, HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, RPOPLPUSH, SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD, SISMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE, SDIFF, SDIFFSTORE, SRANDMEMBER, ZADD, ZRANGE, ZREM, ZINCRBY, ZRANK, ZREVRANK, ZREVRANGE, ZCARD, ZSCORE, MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, BLPOP, BRPOP, AUTH, SUBSCRIBE, PUBLISH, UNSUBSCRIBE, PSUBSCRIBE, PUNSUBSCRIBE, PUBSUB, ZCOUNT, ZRANGEBYSCORE, ZREVRANGEBYSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZUNIONSTORE, ZINTERSTORE, SAVE, BGSAVE, BGREWRITEAOF, LASTSAVE, SHUTDOWN, INFO, MONITOR, SLAVEOF, CONFIG, STRLEN, SYNC, LPUSHX, PERSIST, RPUSHX, ECHO, LINSERT, DEBUG, BRPOPLPUSH, SETBIT, GETBIT, SETRANGE, GETRANGE, EVAL, EVALSHA, SCRIPT, SLOWLOG, OBJECT, BITCOUNT, BITOP, SENTINEL, DUMP, RESTORE, PEXPIRE, PEXPIREAT, PTTL, INCRBYFLOAT, PSETEX, CLIENT, TIME, MIGRATE, HINCRBYFLOAT, SCAN, HSCAN, SSCAN, ZSCAN, WAIT, CLUSTER, ASKING;

		public final byte[]	raw;

		RedisCommand() {
			raw = this.name().getBytes(Encoding.UTF8);
		}
	}
	
	public abstract void writeCommand(byte[] command, byte[]... args) ;

}
