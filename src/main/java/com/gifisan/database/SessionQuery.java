package com.gifisan.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.common.LifeCycleUtil;

public class SessionQuery extends DefaultConnectionProxy {

	private DataBaseQuery	dataBaseQuery	= null;
	
	private QueryParamUtil queryParamUtil = null;

	public SessionQuery(DataBaseContext context) throws SQLException {
		super(context);
		this.dataBaseQuery = context.getDataBaseQuery();
		this.queryParamUtil = context.getQueryParamUtil();
	}

	public List<Map> query(String sql, Object[] params) throws SQLException {
		return (List<Map>) executeQuerySQL(sql, params, null);
	}

	public List<?> query(String sql, Object[] params, Class clazz) throws SQLException {

		return executeQueryCall(sql, params, clazz);
	}

	public List<Map> top(String sql, Object[] params,int limit) throws SQLException {
		sql = dataBaseQuery.getTopSQL(sql);

		params = queryParamUtil.top(limit, params);
		
		return query(sql, params);
	}

	public List<?> top(String sql, Object[] params, Class clazz,int limit) throws SQLException {
		sql = dataBaseQuery.getTopSQL(sql);
		
		params = queryParamUtil.top(limit, params);

		return query(sql, params, clazz);
	}

	
	public List<Map> page(String sql, Object[] params,int offset, int size) throws SQLException {
		sql = dataBaseQuery.getPagingSQL(sql);
		
		params = queryParamUtil.page(offset, size, params);

		return query(sql, params);
	}

	public List<?> page(String sql, Object[] params, Class clazz,int offset, int size) throws SQLException {
		sql = dataBaseQuery.getPagingSQL(sql);
		
		params = queryParamUtil.page(offset, size, params);

		return query(sql, params,clazz);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		DataBaseContext context = new DataBaseContext();
		
		context.start();
		
		SessionQuery query = new SessionQuery(context);
		
		query.open();
		
		List list = query.page("select * from ts_user", null,1,1);
		
		System.out.println(list);
		
		query.close();
		
		LifeCycleUtil.stop(context);
		
	}
	

}
