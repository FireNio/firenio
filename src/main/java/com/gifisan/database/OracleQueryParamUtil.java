package com.gifisan.database;

import java.sql.SQLException;

import com.gifisan.nio.common.ArrayUtil;

public class OracleQueryParamUtil implements QueryParamUtil{

	public Object[] page(int firstResult, int pageSize, Object[] params)
			throws SQLException {
		if (params == null) {
			return new Object[] { firstResult, firstResult + pageSize };
		}
		return ArrayUtil
				.groupArray(params, new Object[] { firstResult, firstResult + pageSize });
	}

	public Object[] top(int size, Object[] params) throws SQLException {
		if (params == null) {
			return new Object[] { size };
		}
		return ArrayUtil.groupArray(params, new Object[] { size });
	}
}
