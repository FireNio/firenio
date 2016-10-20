package com.generallycloud.nio.common.database;

import java.sql.SQLException;

public interface QueryParamUtil {

	Object[] page(int offset, int size, Object[] params) throws SQLException;

	Object[] top(int size, Object[] params) throws SQLException;
}
