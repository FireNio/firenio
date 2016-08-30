package com.generallycloud.nio.common.database;

import java.sql.SQLException;

import com.generallycloud.nio.common.ArrayUtil;

public class MYSQLQueryParamUtil implements QueryParamUtil{

	public Object[] page(int firstResult, int pageSize, Object[] params)
			throws SQLException {
		if (params == null) {
			return new Object[] { firstResult, pageSize };
		}
		return ArrayUtil.groupArray(params, new Object[] { firstResult, pageSize });
	}

	public Object[] top(int size, Object[] params) throws SQLException {
		if (params == null) {
			return new Object[] { size };
		}
		return ArrayUtil.groupArray(params, new Object[] { size });
	}
}
