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
package com.generallycloud.test.others;

import java.util.Set;

import com.generallycloud.baseio.common.CloseUtil;

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

        for (String k : keys) {
            System.out.println("KEY:" + k);
        }

        System.out.println("Stored string in redis:: " + value);

        CloseUtil.close(jedis);

    }
}
