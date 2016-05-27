package com.gifisan.database;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.AbstractLifeCycle;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataBaseContext extends AbstractLifeCycle {

	private DataBaseQuery			dataBaseQuery	= null;

	private ComboPooledDataSource		dataSource	= null;

	private QueryParamUtil			queryParamUtil	= null;

	private Map<String, FieldMapping>	fieldMappings	= new HashMap<String, FieldMapping>();

	public DataBaseQuery getDataBaseQuery() {
		return dataBaseQuery;
	}

	public ComboPooledDataSource getDataSource() {
		return dataSource;
	}

	protected void doStart() throws Exception {

		if (dataSource == null) {

			ComboPooledDataSource dataSource = new ComboPooledDataSource();

			this.dataSource = dataSource;
		}

		String driverClass = dataSource.getDriverClass();

		if (ORACLE_DRIVER_CLASS.equals(driverClass)) {
			this.dataBaseQuery = new OracleQuery();
			this.queryParamUtil = new OracleQueryParamUtil();
		} else if (ORACLE_DRIVER_CLASS.equals(driverClass)) {
			this.dataBaseQuery = new OracleQuery();
		}
	}

	private final String	ORACLE_DRIVER_CLASS	= "oracle.jdbc.driver.OracleDriver";

	protected void doStop() throws Exception {
		if (dataSource != null) {
			dataSource.close();
		}
	}

	public QueryParamUtil getQueryParamUtil() {
		return queryParamUtil;
	}

	public void registBean(String className) throws ClassNotFoundException {

		Class clazz = this.getClass().getClassLoader().loadClass(className);

		this.fieldMappings.put(className, new FieldMapping(clazz));
	}

	public FieldMapping getFieldMapping(String className) {
		return fieldMappings.get(className);
	}

}
