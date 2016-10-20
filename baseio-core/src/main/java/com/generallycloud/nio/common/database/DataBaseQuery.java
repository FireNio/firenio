package com.generallycloud.nio.common.database;

public interface DataBaseQuery {
	
	public abstract String getTopSQL(String sql) ;

	public abstract String getPagingSQL(String sql);

	public abstract String getColumnName(String columnName);

}