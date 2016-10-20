package com.generallycloud.nio.common;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Util {

	public static byte[] SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(decript.getBytes());
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String SHA1(String decript, Charset encoding) {
		byte[] array = SHA1(decript);
		return new String(array, encoding);
	}

	public static void main(String[] args) {

		String ss = "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";

		byte[] s = SHA1(ss);

		System.out.println(s);

		String s1 = BASE64Util.byteArrayToBase64(s);

		System.out.println(s1);
	}

}
