package com.generallycloud.test.nio.others;

import java.util.Set;

import com.generallycloud.nio.common.CloseUtil;

import redis.clients.jedis.Jedis;

public class JedisTest {

	public static void main(String[] args) {

		// 连接本地的 Redis 服务
		Jedis jedis = new Jedis("localhost");
		System.out.println("Connection to server sucessfully");
		// 设置 redis 字符串数据
		jedis.set("k1", "v1");
		jedis.set("k2", "v2");
		// 获取存储的数据并输出
		String value = jedis.get("k1");
		
		Set<String> keys = jedis.keys("*");
		
		for(String k : keys){
			System.out.println("KEY:"+k);
		}
		
		System.out.println("Stored string in redis:: " + value);
		
		CloseUtil.close(jedis);

	}
}
