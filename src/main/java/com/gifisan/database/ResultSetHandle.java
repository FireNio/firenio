package com.gifisan.database;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.common.ClassUtil;

public class ResultSetHandle {

	public List fillData(DataBaseContext context, ResultSet resultSet, Class clazz) throws SQLException {

		if (!resultSet.next()) {
			return null;
		}

		ResultSetMetaData metaData = resultSet.getMetaData();
		int numberOfColumns = metaData.getColumnCount();
		List list = new ArrayList();
		do {
			Object object = fillRow(resultSet, metaData, clazz, numberOfColumns);
			list.add(object);
		} while (resultSet.next());
		return list;

	}

	private Object fillRow(ResultSet resultSet, ResultSetMetaData metaData, Class clazz, int numberOfColumns)
			throws SQLException {
		if (clazz == null) {
			throw new IllegalArgumentException("empty class");
		}

		Object object = ClassUtil.newInstance(clazz);

		if (object == null) {
			throw new SQLException("class can not initialize," + clazz.getName());
		}

		for (int i = 1, n = numberOfColumns + 1; i < n; i++) {
			Object value = resultSet.getObject(i);

			if (value != null) {
				String columnName = metaData.getColumnLabel(i);
				try {
					Field field = clazz.getField(columnName);
					if (field != null) {

						if (!field.isAccessible()) {
							field.setAccessible(true);
						}

						field.set(object, value);
					}
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
		return object;
	}

	public List fillData(DataBaseContext context, ResultSet resultSet) throws SQLException {
		
		if (!resultSet.next()) {
			return null;
		}
		
		ResultSetMetaData metaData = resultSet.getMetaData();
		int numberOfColumns = metaData.getColumnCount();
		List list = new ArrayList<Map>();
		
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