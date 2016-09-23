package com.generallycloud.nio.common.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.ClassUtil;
import com.generallycloud.nio.common.FieldMapping;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;

public class DataBaseContext extends AbstractLifeCycle {

	private DataBaseQuery			dataBaseQuery	;

	private DruidDataSource			dataSource	;

	private QueryParamUtil			queryParamUtil	;
	
	private Properties 				properties	;
	
	private Logger					logger 		= LoggerFactory.getLogger(DataBaseContext.class);

	private Map<String, FieldMapping>	fieldMappings	= new HashMap<String, FieldMapping>();
	
	public DataBaseContext(Properties properties) {
		this.properties = properties;
	}

	public DataBaseQuery getDataBaseQuery() {
		return dataBaseQuery;
	}

	public DruidDataSource getDataSource() {
		return dataSource;
	}

	protected void doStart() throws Exception {

		if (dataSource == null) {
			
			DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
			
			dataSource.init();
			
			LoggerUtil.prettyNIOServerLog(logger, "database context start successful");
			
			this.dataSource = dataSource;
		}

		String driverClass = dataSource.getDriverClassName();

		if (ORACLE_DRIVER_CLASS.equals(driverClass)) {
			this.dataBaseQuery = new OracleQuery();
			this.queryParamUtil = new OracleQueryParamUtil();
		} else if (ORACLE_DRIVER_CLASS.equals(driverClass)) {
			this.dataBaseQuery = new OracleQuery();
		}
		
		this.registBean(COUNT.class);
		
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

		Class clazz = ClassUtil.forName(className);
		
		this.fieldMappings.put(className, new FieldMapping(clazz));
	}
	
	public void registBean(Class clazz){
		if (clazz == null) {
			return;
		}
		
		this.fieldMappings.put(clazz.getName(), new FieldMapping(clazz));
	}

	public FieldMapping getFieldMapping(String className) {
		return fieldMappings.get(className);
	}

}
