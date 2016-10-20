package com.generallycloud.nio.common.database;

import java.util.HashMap;

public class OracleQuery extends AbstractQuery {

	private final String FOOTERPAGE = ")WK$TEMP ) WKTEMP WHERE WKTEMP.ROW_NUM > ? AND WKTEMP.ROW_NUM <= ?";
	private final String FOOTERTOP = ")WK$TEMP ) WKTEMP WHERE WKTEMP.ROW_NUM > 0 AND WKTEMP.ROW_NUM <= ?";
	private final String SHYINHAO = "\"";
	private final String SELECTXFROM = "SELECT WKTEMP.* FROM (SELECT ROWNUM ROW_NUM ,WK$TEMP.* FROM (";

	public String getColumnName(String columnName) {
		if (sys_fields.containsKey(columnName.toUpperCase())) {
			return SHYINHAO + columnName + SHYINHAO;
		} else {
			return columnName;
		}
	}

	public String getPagingSQL(String sql) {
		return new StringBuilder(SELECTXFROM)
			.append(sql)
			.append(FOOTERPAGE)
			.toString();
	}

	public String getTopSQL(String sql) {
		return new StringBuilder(SELECTXFROM)
			.append(sql)
			.append(FOOTERTOP)
			.toString();
	}

	void setSys_fields() {
		sys_fields = new HashMap() {
			private final int temp = 0;
			{
				put("ALL", temp);
				put("ALTER", temp);
				put("AND", temp);
				put("ANY", temp);
				put("AS", temp);
				put("ASC", temp);
				put("BETWEEN", temp);
				put("BY", temp);
				put("CHAR", temp);
				put("CHECK", temp);
				put("CLUSTER", temp);
				put("COMPRESS", temp);
				put("CONNECT", temp);
				put("CREATE", temp);
				put("DATE", temp);
				put("DECIMAL", temp);
				put("DEFAULT", temp);
				put("DELETE", temp);
				put("DESC", temp);
				put("DISTINCT", temp);
				put("DROP", temp);
				put("ELSE", temp);
				put("EXCLUSIVE", temp);
				put("EXISTS", temp);
				put("FLOAT", temp);
				put("FOR", temp);
				put("FROM", temp);
				put("GRANT", temp);
				put("GROUP", temp);
				put("HAVING", temp);
				put("IDENTIFIED", temp);
				put("IN", temp);
				put("INDEX", temp);
				put("INSERT", temp);
				put("INTEGER", temp);
				put("INTERSECT", temp);
				put("INTO", temp);
				put("IS", temp);
				put("LIKE", temp);
				put("LOCK", temp);
				put("LONG", temp);
				put("MINUS", temp);
				put("MODE", temp);
				put("NOCOMPRESS", temp);
				put("NOT", temp);
				put("NOWAIT", temp);
				put("NULL", temp);
				put("NUMBER", temp);
				put("OF", temp);
				put("ON", temp);
				put("OPTION", temp);
				put("OR", temp);
				put("ORDER", temp);
				put("PCTFREE", temp);
				put("PRIOR", temp);
				put("PUBLIC", temp);
				put("RAW", temp);
				put("RENAME", temp);
				put("RESOURCE", temp);
				put("REVOKE", temp);
				put("SELECT", temp);
				put("SET", temp);
				put("SHARE", temp);
				put("SIZE", temp);
				put("SMALLINT", temp);
				put("START", temp);
				put("SYNONYM", temp);
				put("TABLE", temp);
				put("THEN", temp);
				put("TO", temp);
				put("TRIGGER", temp);
				put("UNION", temp);
				put("UNIQUE", temp);
				put("UPDATE", temp);
				put("VALUES", temp);
				put("VARCHAR", temp);
				put("VARCHAR2", temp);
				put("VIEW", temp);
				put("WHERE", temp);
				put("WITH", temp);
			}
		};
	}
}