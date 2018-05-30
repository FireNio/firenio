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
package com.generallycloud.baseio.common;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author wangkai
 *
 */
public class AESUtil {

    public static String encrypt(String content, String password) throws Exception {
        return encrypt(content, password, Encoding.UTF8);
    }

    public static String encrypt(String content, String password, Charset charset)
            throws Exception {
        byte[] contentBytes = content.getBytes(charset);
        byte[] passwordBytes = password.getBytes(charset);
        byte[] res = encrypt(contentBytes, passwordBytes);
        return MathUtil.bytes2HexString(res);
    }

    public static byte[] encrypt(byte[] content, byte[] password) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");// 创建AES的Key生产者
        // 利用用户密码作为随机数初始化出,128位的key生产者
        //加密没关系，SecureRandom是生成安全随机数序列，password.getBytes()是种子，只要种子相同，序列就一样，所以解密只要有password就行
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password);
        kgen.init(128, random);
        SecretKey secretKey = kgen.generateKey();// 根据用户密码，生成一个密钥
        // 返回基本编码格式的密钥，如果此密钥不支持编码，则返回null
        byte[] enCodeFormat = secretKey.getEncoded();
        // 转换为AES专用密钥
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        // 初始化为加密模式的密码器
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // 加密
        return cipher.doFinal(content);
    }

    public static byte[] decrypt(byte[] content, byte[] password) throws Exception {
        // 创建AES的Key生产者
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(password);
        kgen.init(128, random);
        // 根据用户密码，生成一个密钥
        SecretKey secretKey = kgen.generateKey();
        // 返回基本编码格式的密钥
        byte[] enCodeFormat = secretKey.getEncoded();
        // 转换为AES专用密钥
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES");
        // 初始化为解密模式的密码器
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(content);
    }

    public static String decrypt(String content, String password) throws Exception {
        return decrypt(content, password, Encoding.UTF8);
    }

    public static String decrypt(String content, String password, Charset charset)
            throws Exception {
        byte[] contentBytes = MathUtil.hexString2bytes(StringUtil.stringToCharArray(content));
        byte[] passowrdBytes = password.getBytes(charset);
        byte[] res = decrypt(contentBytes, passowrdBytes);
        return new String(res, charset);
    }

    public static void main(String[] args) throws Exception {

        String text = "test";
        String key = "key";
        System.out.println("TEXT:" + text);
        System.out.println("KEY:" + key);
        String mi = encrypt(text, key);
        System.out.println("MI:" + mi);
        text = decrypt(mi, key);
        System.out.println("TEXT:" + text);

    }

}
