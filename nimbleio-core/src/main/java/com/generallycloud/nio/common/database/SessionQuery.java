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

	public List<?> top(String sql, Object[] params, Class clazz,int limit) throws SQLException {
		sql = dataBaseQuery.getTopSQL(sql);
		
		params = queryParamUtil.top(limit, params);

		return query(sql, params, clazz);
	}

	
	public List<Map<String, Object>> page(String sql, Object[] params,int offset, int size) throws SQLException {
		sql = dataBaseQuery.getPagingSQL(sql);
		
		params = queryParamUtil.page(offset, size, params);

		return query(sql, params);
	}

	public List<?> page(String sql, Object[] params, Class clazz,int offset, int size) throws SQLException {
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
