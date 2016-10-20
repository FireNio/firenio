package com.generallycloud.nio.common;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import com.generallycloud.nio.Encoding;

public class RSAUtil {

	private static KeyFactory	keyFactory;

	static {
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
		}
	}

	/**
	 * 生成公钥和私钥
	 * 
	 * @param length
	 *             1024 ...
	 * @throws NoSuchAlgorithmException
	 *
	 */
	public static RSAKeys getKeys(int length) throws NoSuchAlgorithmException {

		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

		keyPairGen.initialize(length);

		KeyPair keyPair = keyPairGen.generateKeyPair();

		RSAKeys keys = new RSAKeys();

		keys.publicKey = (RSAPublicKey) keyPair.getPublic();
		keys.privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return keys;
	}

	/**
	 * 使用模和指数生成RSA公钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
	 * /None/NoPadding】
	 * 
	 * @param modulus
	 *             模
	 * @param exponent
	 *             指数
	 * @return
	 */
	public static RSAPublicKey getPublicKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 使用模和指数生成RSA私钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
	 * /None/NoPadding】
	 * 
	 * @param modulus
	 *             模
	 * @param exponent
	 *             指数
	 * @return
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, RSAPublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		// 模长
		int key_len = publicKey.getModulus().bitLength() / 8;
		// 加密数据长度 <= 模长-11

		List<byte[]> datas = splitArray(data, key_len - 11);

		ByteBuffer buffer = ByteBuffer.allocate(key_len * datas.size());

		// 如果明文长度大于模长-11则要分组加密
		for (byte[] s : datas) {

			byte[] array = cipher.doFinal(s);

			buffer.put(array);

		}
		return buffer.array();
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] bytes, RSAPrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		// 模长
		int key_len = privateKey.getModulus().bitLength() / 8;
		// 如果密文长度大于模长则要分组解密

		List<byte[]> arrays = splitArray(bytes, key_len);

		ByteBuffer buffer = ByteBuffer.allocate(arrays.size() * (key_len - 11));

		for (byte[] arr : arrays) {
			byte[] marr = cipher.doFinal(arr);
			buffer.put(marr);
		}

		byte[] r = new byte[buffer.position()];
		
		buffer.flip();

		buffer.get(r);

		return r;
	}

	/**
	 * 拆分字符串
	 */
	private static List<byte[]> splitArray(byte[] array, int len) {

		int length = array.length;

		List<byte[]> list = new ArrayList<byte[]>();

		if (length <= len) {

			list.add(array);

			return list;
		}

		int size = length / len;

		for (int i = 0; i < size; i++) {
			byte[] a = new byte[len];
			System.arraycopy(array, i * len, a, 0, len);
			list.add(a);
		}

		int yu = array.length % len;

		if (yu > 0) {
			byte[] a = new byte[yu];
			System.arraycopy(array, length - yu, a, 0, yu);
			list.add(a);
		}

		return list;
	}

	public static class RSAKeys {

		private RSAPublicKey	publicKey;
		private RSAPrivateKey	privateKey;

		public RSAPublicKey getPublicKey() {
			return publicKey;
		}

		public RSAPrivateKey getPrivateKey() {
			return privateKey;
		}

		public void setPublicKey(RSAPublicKey publicKey) {
			this.publicKey = publicKey;
		}

		public void setPrivateKey(RSAPrivateKey privateKey) {
			this.privateKey = privateKey;
		}
	}

	public static void generateKeys(String file, int length) throws NoSuchAlgorithmException, IOException {
		RSAKeys keys = RSAUtil.getKeys(length);
		// 生成公钥和私钥
		RSAPublicKey publicKey = keys.getPublicKey();
		RSAPrivateKey privateKey = keys.getPrivateKey();

		File publicKeyFile = new File(file + "/public.rsa");
		String publicKeyString = publicKey.toString();

		File privateKeyFile = new File(file + "/private.rsa");
		String privateKeyString = privateKey.toString();

		FileUtil.write(publicKeyFile, publicKeyString);
		FileUtil.write(privateKeyFile, privateKeyString);

		System.out.println("Public RSA File:" + publicKeyFile.getCanonicalPath());
		System.out.println(publicKeyString);
		System.out.println();
		System.out.println("Private RSA File:" + privateKeyFile.getCanonicalPath());
		System.out.println(privateKeyString);
	}

	private static Map<String, String> parseRSAFromContent(String content) {
		String[] lines = content.split("\n");
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 1; i < lines.length; i++) {
			String[] array = lines[i].split(":");
			if (array.length != 2) {
				continue;
			}
			String name = array[0].trim().replace("\r", "");
			String value = array[1].trim().replace("\r", "");
			map.put(name, value);
		}
		return map;
	}

	public static RSAPublicKey getRsaPublicKey(String content) {

		if (StringUtil.isNullOrBlank(content)) {
			throw new IllegalArgumentException("null content");
		}

		Map<String, String> map = parseRSAFromContent(content);

		String modulus = map.get("modulus");
		String exponent = map.get("public exponent");

		return getPublicKey(modulus, exponent);
	}

	public static RSAPrivateKey getRsaPrivateKey(String content) {

		if (StringUtil.isNullOrBlank(content)) {
			throw new IllegalArgumentException("null content");
		}

		Map<String, String> map = parseRSAFromContent(content);

		String modulus = map.get("modulus");
		String exponent = map.get("private exponent");

		return getPrivateKey(modulus, exponent);
	}

	public static void main(String[] args) throws Exception {
		RSAKeys keys = RSAUtil.getKeys(1024);
		// 生成公钥和私钥
		RSAPublicKey publicKey = keys.getPublicKey();
		RSAPrivateKey privateKey = keys.getPrivateKey();

		// 模
		String modulus = publicKey.getModulus().toString();
		// 公钥指数
		String public_exponent = publicKey.getPublicExponent().toString();
		// 私钥指数
		String private_exponent = privateKey.getPrivateExponent().toString();
		// 明文
		String ming = "你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好";
		// ming = "22ttt2aasdaaaaaasdasddw";
		// 使用模和指数生成公钥和私钥
		RSAPublicKey pubKey = RSAUtil.getPublicKey(modulus, public_exponent);
		RSAPrivateKey priKey = RSAUtil.getPrivateKey(modulus, private_exponent);
		// 加密后的密文
		byte[] mi = RSAUtil.encryptByPublicKey(ming.getBytes(Encoding.GBK), pubKey);
		System.out.println("mi.length:" + mi.length);
		// 解密后的明文
		ming = new String(RSAUtil.decryptByPrivateKey(mi, priKey),Encoding.GBK);
		System.err.println("明文：" + ming);
	}
}
