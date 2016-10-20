package com.generallycloud.nio.common.database;

import java.sql.SQLException;
import java.util.List;

public interface ConnectionProxy {

	public abstract void beginTransaction() throws SQLException;
	
	public abstract void open() throws SQLException ;

	public abstract void commit() throws SQLException;

	public abstract <T> List<T> executeQueryCall(String sql, Object[] params, Class<T> clazz) throws SQLException;

	public abstract <T> List<T> executeQuerySQL(String sql, Object[] params, Class<T> clazz) throws SQLException;
	
	public abstract int executeUpdateCall(String sql, Object[] params) throws SQLException;

	public abstract int executeUpdateSQL(String sql, Object[] params) throws SQLException;

	public abstract void close();

	public abstract void rollback() throws SQLException;

}