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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SessionQuery extends DefaultConnectionProxy {

	private DataBaseQuery	dataBaseQuery	;
	
	private QueryParamUtil queryParamUtil = null;

	public SessionQuery(DataBaseContext context) {
		super(context);
		this.dataBaseQuery = context.getDataBaseQuery();
		this.queryParamUtil = context.getQueryParamUtil();
	}

	public List<Map<String, Object>> query(String sql, Object[] params) throws SQLException {
		return executeQuerySQL(sql, params, null);
	}

	public <T> List<T> query(String sql, Object[] params, Class<T> clazz) throws SQLException {

		return executeQuerySQL(sql, params, clazz);
	}

	public List<Map<String, Object>> top(String sql, Object[] params,int limit) throws SQLException {
		sql = dataBaseQuery.getTopSQL(sql);

		params = queryParamUtil.top(limit, params);
		
		return query(sql, params);
	}

	public <T> List<T> top(String sql, Object[] params, Class<T> clazz,int limit) throws SQLException {
		sql = dataBaseQuery.getTopSQL(sql);
		
		params = queryParamUtil.top(limit, params);

		return query(sql, params, clazz);
	}

	
	public List<Map<String, Object>> page(String sql, Object[] params,int offset, int size) throws SQLException {
		sql = dataBaseQuery.getPagingSQL(sql);
		
		params = queryParamUtil.page(offset, size, params);

		return query(sql, params);
	}

	public <T> List<T> page(String sql, Object[] params, Class<T> clazz,int offset, int size) throws SQLException {
		sql = dataBaseQuery.getPagingSQL(sql);
		
		params = queryParamUtil.page(offset, size, params);

		return query(sql, params,clazz);
	}
	
	/**
	 * 
	 * @param sql select count(1) count from table ... 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public long queryCount(String sql, Object[] params) throws SQLException{
		
		List<COUNT> counts = query(sql, params, COUNT.class);
		
		return counts.get(0).getCount();
	}
	

}
