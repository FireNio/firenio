/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.common.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class DefaultConnectionProxy implements ConnectionProxy {

	private Connection		connection		;
	private DataBaseContext	context			;
	private Logger			logger			= Logger.getLogger(DefaultConnectionProxy.class);
	private ResultSetHandle	resultSetHandle	= new ResultSetHandle();
	private AtomicBoolean	closed			= new AtomicBoolean(true);

	public DefaultConnectionProxy(DataBaseContext context) {
		this.context = context;
	}

	@Override
	public void open() throws SQLException {
		if (closed.compareAndSet(true, false)) {
			this.connection = context.getDataSource().getConnection();
		}
	}
	
	private void checkConnection() throws SQLException{
		if (closed.get()) {
			throw new SQLException("closed");
		}
	}

	@Override
	public void beginTransaction() throws SQLException {
		checkConnection();
		connection.setAutoCommit(false);
	}

	private void closeStatementForQuery(CallableStatement callableStatement, PreparedStatement preparedStatement,
			ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
				resultSet = null;
			}
			if (preparedStatement != null) {
				preparedStatement.close();
				preparedStatement = null;
			}
			if (callableStatement != null) {
				callableStatement.close();
				callableStatement = null;
			}
		} catch (SQLException e) {
			logger.debug(logger, e);
		}
	}

	private void closeStatementForUpdate(CallableStatement callableStatement, PreparedStatement preparedStatement) {
		try {
			if (preparedStatement != null) {
				preparedStatement.close();
				preparedStatement = null;
			}
			if (callableStatement != null) {
				callableStatement.close();
				callableStatement = null;
			}
		} catch (SQLException e) {
			logger.debug(logger, e);
		}
	}

	@Override
	public void commit() throws SQLException {
		
		checkConnection();
		
		try {
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		connection.setAutoCommit(true);
	}

	private void debugSQLExecute(final String sql, Object[] params) {
		if (logger.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder("execute sql:");
			builder.append(sql);
			builder.append(" >>> params[");
			if (params != null && params.length > 0) {
				int length = params.length - 1;
				for (int i = 0; i < length; i++) {
					builder.append(params[i]);
					builder.append(" , ");
				}
				builder.append(params[params.length - 1]);
			}
			builder.append("]");
			logger.debug(builder.toString());
		}
	}

	@Override
	public <T> List<T> executeQueryCall(final String sql, final Object[] params, Class<T> clazz) throws SQLException {
		this.debugSQLExecute(sql, params);
		checkConnection();
		
		PreparedStatement preparedStatement = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		List<T> list = null;
		try {
			callableStatement = connection.prepareCall(sql);
			registCSPram(params, callableStatement);
			resultSet = callableStatement.executeQuery();
			registCSOutPram(params, callableStatement);
			list = fillData(context, resultSet, clazz);
		} finally {
			closeStatementForQuery(callableStatement, preparedStatement, resultSet);
		}
		return list;
	}

	@Override
	public <T> List<T> executeQuerySQL(final String sql, final Object[] params, Class<T> clazz) throws SQLException {
		this.debugSQLExecute(sql, params);
		checkConnection();
		PreparedStatement preparedStatement = null;
		CallableStatement callableStatement = null;
		ResultSet resultSet = null;
		List<T> list = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			registPSPram(params, preparedStatement);
			resultSet = preparedStatement.executeQuery();
			list = fillData(context, resultSet, clazz);
		} finally {
			closeStatementForQuery(callableStatement, preparedStatement, resultSet);
		}
		return list;
	}

	@Override
	public int executeUpdateCall(final String sql, final Object[] params) throws SQLException {
		this.debugSQLExecute(sql, params);
		checkConnection();
		PreparedStatement preparedStatement = null;
		CallableStatement callableStatement = null;
		int count = 0;
		try {
			callableStatement = connection.prepareCall(sql);
			registCSPram(params, callableStatement);
			count = callableStatement.executeUpdate();
			registCSOutPram(params, callableStatement);
		} finally {
			closeStatementForUpdate(callableStatement, preparedStatement);
		}
		return count;
	}

	@Override
	public int executeUpdateSQL(final String sql, final Object[] params) throws SQLException {
		this.debugSQLExecute(sql, params);
		checkConnection();
		PreparedStatement preparedStatement = null;
		CallableStatement callableStatement = null;
		int count = 0;
		try {
			preparedStatement = connection.prepareStatement(sql);
			registPSPram(params, preparedStatement);
			count = preparedStatement.executeUpdate();

		} finally {
			closeStatementForUpdate(callableStatement, preparedStatement);
		}
		return count;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> List<T> fillData(DataBaseContext context, ResultSet resultSet, Class clazz) throws SQLException {
		if (clazz == null) {
			return resultSetHandle.fillData(context, resultSet);
		}
		return resultSetHandle.fillData(context, resultSet, clazz);
	}

	private void registCSOutPram(Object[] params, CallableStatement callableStatement) throws SQLException {
		if (params != null && params.length > 0) {
			for (int i = 0, n = params.length; i < n; i++) {
				if (params[i] == null)
					params[i] = callableStatement.getObject(i + 1);
			}
		}
	}

	private void registCSPram(Object[] params, CallableStatement callableStatement) throws SQLException {
		if (params != null && params.length > 0) {
			for (int i = 0, n = params.length; i < n; i++) {
				if (params[i] != null)
					callableStatement.setObject(i + 1, params[i]);
				else
					callableStatement.registerOutParameter(i + 1, java.sql.Types.INTEGER);
			}
		}
	}

	private void registPSPram(Object[] params, PreparedStatement preparedStatement) throws SQLException {
		if (params != null && params.length > 0) {
			for (int i = 0, n = params.length; i < n; i++) {
				preparedStatement.setObject(i + 1, params[i]);
			}
		}
	}

	@Override
	public void close() {
		if (this.closed.compareAndSet(false, true)) {
			
			if (connection == null) {
				return;
			}
			
			try {
				this.connection.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			
			this.connection = null;
		}
	}

	@Override
	public void rollback() throws SQLException {
		checkConnection();
		connection.rollback();
	}
}
