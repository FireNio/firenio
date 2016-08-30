package com.generallycloud.nio.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class BeanUtil {

	private static Map<Class, FieldMapping>	fieldMapping	= new HashMap<Class, FieldMapping>();
	private static ReentrantLock			lock				= new ReentrantLock();
	
	public static Object map2Object(Map<String, Object> map, Class clazz) {
		if (map == null || clazz == null) {
			return null;
		}

		Object object = ClassUtil.newInstance(clazz);

		FieldMapping mapping = fieldMapping.get(object.getClass());

		if (mapping == null) {

			ReentrantLock lock = BeanUtil.lock;

			lock.lock();

			mapping = fieldMapping.get(object.getClass());

			if (mapping == null) {

				mapping = new FieldMapping(object.getClass());

				fieldMapping.put(object.getClass(), mapping);
			}

			lock.unlock();
		}

		List<Field> fieldList = mapping.getFieldList();

		for (Field f : fieldList) {

			Object v = map.get(f.getName());

			if (v == null) {
				continue;
			}

			if (!f.isAccessible()) {
				f.setAccessible(true);
			}

			try {
				f.set(object, v);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return object;
	}

}
