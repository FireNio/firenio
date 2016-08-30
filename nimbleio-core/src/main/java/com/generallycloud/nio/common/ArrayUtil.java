package com.generallycloud.nio.common;

/**
 * 
 * 数组的帮助类
 * 
 */
public class ArrayUtil {

	/**
	 * 把两个Object[] 转成一个
	 * 
	 * @param param1
	 * @param param2
	 * @return Object[]
	 */
	public static Object[] groupArray(Object[] param1, Object[] param2) {
		Object[] param = new Object[param1.length + param2.length];
		System.arraycopy(param1, 0, param, 0, param1.length);
		System.arraycopy(param2, 0, param, param1.length, param2.length);
		return param;
	}
}
