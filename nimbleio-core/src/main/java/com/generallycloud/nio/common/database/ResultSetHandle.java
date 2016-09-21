package com.generallycloud.nio.common.database;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.generallycloud.nio.common.ClassUtil;
import com.generallycloud.nio.common.FieldMapping;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ResultSetHandle {

	public List fillData(DataBaseContext context, ResultSet resultSet, Class clazz) throws SQLException {

		if (!resultSet.next()) {
			return null;
		}

		List list = new ArrayList();

		FieldMapping mapping = context.getFieldMapping(clazz.getName());

		do {

			Object object = fillRow(resultSet, mapping);

			list.add(object);
		} while (resultSet.next());

		return list;
	}

	private Object fillRow(ResultSet resultSet, FieldMapping mapping) throws SQLException {

		ResultSetMetaData metaData = resultSet.getMetaData();

		Class clazz = mapping.getMappingClass();

		int numberOfColumns = metaData.getColumnCount();

		Object object = ClassUtil.newInstance(clazz);

		if (object == null) {
			throw new SQLException("class can not initialize," + clazz.getName());
		}

		for (int i = 1, n = numberOfColumns + 1; i < n; i++) {
			Object value = resultSet.getObject(i);

			if (value != null) {
				String columnName = metaData.getColumnLabel(i);
				try {
					Field field = mapping.getField(columnName);
					if (field != null) {

						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						
						value = o2t(value, field.getType());
						
						field.set(object, value);
					}
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
		return object;
	}

	private Object o2t(Object src, Type t) throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {

		if (src == null) {
			return null;
		}
		
		if (t == Boolean.TYPE || t == Boolean.class) {

			if (src instanceof Number) {

				return ((Number) src).intValue() == 1;
			}

			return Boolean.valueOf(src.toString());
		}
		
		String val = src.toString();

		if (t == java.lang.String.class) {
			return val;
		}
		if (t == Short.TYPE || t == Short.class) {
			return Short.valueOf(val);
		}
		if (t == Integer.TYPE || t == Integer.class) {
			return Integer.valueOf(val);
		}
		if (t == Long.TYPE || t == Long.class) {
			return Long.valueOf(val);
		}
		if (t == Double.TYPE || t == Double.class) {
			return Double.valueOf(val);
		}
		
		if (t == Float.TYPE || t == Float.class) {
			return Float.valueOf(val);
		}
		if (t == Byte.TYPE || t == Byte.class) {
			return Byte.valueOf(val);
		}
		if (t == Character.TYPE || t == Character.class) {
			return val.charAt(0);
		}
		if (t == java.math.BigDecimal.class) {
			return new BigDecimal(val);
		}
		
		if (t.getClass() == ParameterizedTypeImpl.class) {

			ParameterizedTypeImpl type = (ParameterizedTypeImpl) t;

			if (type.getActualTypeArguments() == null || type.getActualTypeArguments().length == 0) {
				throw new IllegalArgumentException("未指定泛型，" + src);
			}

			Class clazz = (Class) type.getRawType();

			Class actualType = (Class) type.getActualTypeArguments()[0];

			if (List.class.isAssignableFrom(clazz)) {
				return string2list(actualType, (List<Map>) src);
			} else {
				throw new IllegalArgumentException("非法的类型，" + clazz);
			}
		}
		return null;
		// TODO 完善其他类型
		 
	}

	
	private List string2list(Class actualType, List<Map> src) throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {
		List list = new ArrayList();
		Object obj = actualType.newInstance();
		for (Map map : src) {
			map2bean(obj.getClass(), obj, map);
			list.add(obj);
		}
		return list;
	}

	private void map2bean(Class clazz, Object obj, Map map) throws IllegalArgumentException, IllegalAccessException,
			InstantiationException {

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {

			Object _value = map.get(field.getName());

			Object value = o2t(_value, field.getGenericType());

			field.setAccessible(true);

			field.set(obj, value);

			field.setAccessible(false);
		}
	}

	public List fillData(DataBaseContext context, ResultSet resultSet) throws SQLException {

		if (!resultSet.next()) {
			return null;
		}

		ResultSetMetaData metaData = resultSet.getMetaData();

		List list = new ArrayList<Map>();

		int numberOfColumns = metaData.getColumnCount();

		do {

			Map<String, Object> rsTree = new HashMap<String, Object>(numberOfColumns);

			for (int i = 1, n = numberOfColumns + 1; i < n; i++) {

				rsTree.put(metaData.getColumnLabel(i), resultSet.getObject(i));

			}

			list.add(rsTree);

		} while (resultSet.next());

		return list;
	}
}