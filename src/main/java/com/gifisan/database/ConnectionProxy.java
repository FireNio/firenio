package com.gifisan.database;

import java.sql.SQLException;
import java.util.List;

public interface ConnectionProxy {

	public abstract void beginTransaction() throws SQLException;
	
	public abstract void open() throws SQLException ;

	public abstract void commit() throws SQLException;

	public abstract List<?> executeQueryCall(String sql, Object[] params, Class<?> clazz) throws SQLException;

	public abstract List<?> executeQuerySQL(String sql, Object[] params, Class<?> clazz) throws SQLException;
	
	public abstract int executeUpdateCall(String sql, Object[] params) throws SQLException;

	public abstract int executeUpdateSQL(String sql, Object[] params) throws SQLException;

	public abstract void close();

	public abstract void rollback() throws SQLException;

}