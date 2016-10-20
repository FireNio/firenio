package com.generallycloud.nio.common;

import java.util.Collection;
import java.util.Map;

public class CollectionUtil {

	public static boolean isEmpty(Collection collection) {
		return collection == null || collection.size() == 0;
	}
	
	public static boolean isEmpty(Object []array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(Map map) {
		return map == null || map.size() == 0;
	}
	

}
