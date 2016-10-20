package com.generallycloud.nio.common.database;

import java.util.HashMap;

public class MySQLQuery extends AbstractQuery {

	private final String LIMIT = " LIMIT ?,?";
	private final String LIMITTOP = " LIMIT 0,?";


	public String getPagingSQL(String sql) {
		return new StringBuilder(sql.length() + 10)
			.append(sql)
			.append(LIMIT)
			.toString();
	}

//	public String getTopSQLByTableName(String tableName) {
//		return new StringBuilder(tableName.length() + 24)
//			.append("SELECT * FROM ")
//			.append(tableName)
//			.append(LIMITTOP)
//			.toString();
//	}

	public String getTopSQL(String sql) {
		return new StringBuilder(sql.length() + 10)
			.append(sql)
			.append(LIMITTOP)
			.toString();
	}

	void setSys_fields() {
		sys_fields = new HashMap<String, String>() {
			{
				put("ACTION", "`ACTION`");
				put("ADD", "`ADD`");
				put("AGGREGATE", "`AGGREGATE`");
				put("ALL", "`ALL`");
				put("ALTER", "`ALTER`");
				put("AFTER", "`AFTER`");
				put("AND", "`AND`");
				put("AS", "`AS`");
				put("ASC", "`ASC`");
				put("AVG", "`AVG`");
				put("AVG_ROW_LENGTH", "`AVG_ROW_LENGTH`");
				put("AUTO_INCREMENT", "`AUTO_INCREMENT`");
				put("BETWEEN", "`BETWEEN`");
				put("BIGINT", "`BIGINT`");
				put("BIT", "`BIT`");
				put("BINARY", "`BINARY`");
				put("BLOB", "`BLOB`");
				put("BOOL", "`BOOL`");
				put("BOTH", "`BOTH`");
				put("BY", "`BY`");
				put("CASCADE", "`CASCADE`");
				put("CASE", "`CASE`");
				put("CHAR", "`CHAR`");
				put("CHARACTER", "`CHARACTER`");
				put("CHANGE", "`CHANGE`");
				put("CHECK", "`CHECK`");
				put("CHECKSUM", "`CHECKSUM`");
				put("COLUMN", "`COLUMN`");
				put("COLUMNS", "`COLUMNS`");
				put("COMMENT", "`COMMENT`");
				put("CONSTRAINT", "`CONSTRAINT`");
				put("CREATE", "`CREATE`");
				put("CROSS", "`CROSS`");
				put("CURRENT_DATE", "`CURRENT_DATE`");
				put("CURRENT_TIME", "`CURRENT_TIME`");
				put("CURRENT_TIMESTAMP", "`CURRENT_TIMESTAMP`");
				put("DATA", "`DATA`");
				put("DATABASE", "`DATABASE`");
				put("DATABASES", "`DATABASES`");
				put("DATE", "`DATE`");
				put("DATETIME", "`DATETIME`");
				put("DAY", "`DAY`");
				put("DAY_HOUR", "`DAY_HOUR`");
				put("DAY_MINUTE", "`DAY_MINUTE`");
				put("DAY_SECOND", "`DAY_SECOND`");
				put("DAYOFMONTH", "`DAYOFMONTH`");
				put("DAYOFWEEK", "`DAYOFWEEK`");
				put("DAYOFYEAR", "`DAYOFYEAR`");
				put("DEC", "`DEC`");
				put("DECIMAL", "`DECIMAL`");
				put("DEFAULT", "`DEFAULT`");
				put("DELAYED", "`DELAYED`");
				put("DELAY_KEY_WRITE", "`DELAY_KEY_WRITE`");
				put("DELETE", "`DELETE`");
				put("DESC", "`DESC`");
				put("DESCRIBE", "`DESCRIBE`");
				put("DISTINCT", "`DISTINCT`");
				put("DISTINCTROW", "`DISTINCTROW`");
				put("DOUBLE", "`DOUBLE`");
				put("DROP", "`DROP`");
				put("END", "`END`");
				put("ELSE", "`ELSE`");
				put("ESCAPE", "`ESCAPE`");
				put("ESCAPED", "`ESCAPED`");
				put("ENCLOSED", "`ENCLOSED`");
				put("ENUM", "`ENUM`");
				put("EXPLAIN", "`EXPLAIN`");
				put("EXISTS", "`EXISTS`");
				put("FIELDS", "`FIELDS`");
				put("FILE", "`FILE`");
				put("FIRST", "`FIRST`");
				put("FLOAT", "`FLOAT`");
				put("FLOAT4", "`FLOAT4`");
				put("FLOAT8", "`FLOAT8`");
				put("FLUSH", "`FLUSH`");
				put("FOREIGN", "`FOREIGN`");
				put("FROM", "`FROM`");
				put("FOR", "`FOR`");
				put("FULL", "`FULL`");
				put("FUNCTION", "`FUNCTION`");
				put("GLOBAL", "`GLOBAL`");
				put("GRANT", "`GRANT`");
				put("GRANTS", "`GRANTS`");
				put("GROUP", "`GROUP`");
				put("HAVING", "`HAVING`");
				put("HEAP", "`HEAP`");
				put("HIGH_PRIORITY", "`HIGH_PRIORITY`");
				put("HOUR", "`HOUR`");
				put("HOUR_MINUTE", "`HOUR_MINUTE`");
				put("HOUR_SECOND", "`HOUR_SECOND`");
				put("HOSTS", "`HOSTS`");
				put("IDENTIFIED", "`IDENTIFIED`");
				put("IGNORE", "`IGNORE`");
				put("IN", "`IN`");
				put("INDEX", "`INDEX`");
				put("INFILE", "`INFILE`");
				put("INNER", "`INNER`");
				put("INSERT", "`INSERT`");
				put("INSERT_ID", "`INSERT_ID`");
				put("INT", "`INT`");
				put("INTEGER", "`INTEGER`");
				put("INTERVAL", "`INTERVAL`");
				put("INT1", "`INT1`");
				put("INT2", "`INT2`");
				put("INT3", "`INT3`");
				put("INT4", "`INT4`");
				put("INT8", "`INT8`");
				put("INTO", "`INTO`");
				put("IF", "`IF`");
				put("IS", "`IS`");
				put("ISAM", "`ISAM`");
				put("JOIN", "`JOIN`");
				put("KEY", "`KEY`");
				put("KEYS", "`KEYS`");
				put("KILL", "`KILL`");
				put("LAST_INSERT_ID", "`LAST_INSERT_ID`");
				put("LEADING", "`LEADING`");
				put("LEFT", "`LEFT`");
				put("LEVEL", "`LEVEL`");
				put("LENGTH", "`LENGTH`");
				put("LIKE", "`LIKE`");
				put("LINES", "`LINES`");
				put("LIMIT", "`LIMIT`");
				put("LOAD", "`LOAD`");
				put("LOCAL", "`LOCAL`");
				put("LOCK", "`LOCK`");
				put("LOGS", "`LOGS`");
				put("LONG", "`LONG`");
				put("LONGBLOB", "`LONGBLOB`");
				put("LONGTEXT", "`LONGTEXT`");
				put("LOW_PRIORITY", "`LOW_PRIORITY`");
				put("MAX", "`MAX`");
				put("MAX_ROWS", "`MAX_ROWS`");
				put("MATCH", "`MATCH`");
				put("MEDIUMBLOB", "`MEDIUMBLOB`");
				put("MEDIUMTEXT", "`MEDIUMTEXT`");
				put("MEDIUMINT", "`MEDIUMINT`");
				put("MIDDLEINT", "`MIDDLEINT`");
				put("MIN_ROWS", "`MIN_ROWS`");
				put("MINUTE", "`MINUTE`");
				put("MINUTE_SECOND", "`MINUTE_SECOND`");
				put("MODIFY", "`MODIFY`");
				put("MONTH", "`MONTH`");
				put("MONTHNAME", "`MONTHNAME`");
				put("MYISAM", "`MYISAM`");
				put("NATURAL", "`NATURAL`");
				put("NUMERIC", "`NUMERIC`");
				put("NUMBER", "`NUMBER`");
				put("NO", "`NO`");
				put("NOT", "`NOT`");
				put("NULL", "`NULL`");
				put("ON", "`ON`");
				put("OPTIMIZE", "`OPTIMIZE`");
				put("OPTION", "`OPTION`");
				put("OPTIONALLY", "`OPTIONALLY`");
				put("OR", "`OR`");
				put("ORDER", "`ORDER`");
				put("OUTER", "`OUTER`");
				put("OUTFILE", "`OUTFILE`");
				put("PACK_KEYS", "`PACK_KEYS`");
				put("PARTIAL", "`PARTIAL`");
				put("PASSWORD", "`PASSWORD`");
				put("PRECISION", "`PRECISION`");
				put("PRIMARY", "`PRIMARY`");
				put("PROCEDURE", "`PROCEDURE`");
				put("PROCESS", "`PROCESS`");
				put("PROCESSLIST", "`PROCESSLIST`");
				put("PRIVILEGES", "`PRIVILEGES`");
				put("READ", "`READ`");
				put("REAL", "`REAL`");
				put("REFERENCES", "`REFERENCES`");
				put("RELOAD", "`RELOAD`");
				put("REGEXP", "`REGEXP`");
				put("RENAME", "`RENAME`");
				put("REPLACE", "`REPLACE`");
				put("RESTRICT", "`RESTRICT`");
				put("RETURNS", "`RETURNS`");
				put("REVOKE", "`REVOKE`");
				put("RLIKE", "`RLIKE`");
				put("ROW", "`ROW`");
				put("ROWS", "`ROWS`");
				put("SECOND", "`SECOND`");
				put("SELECT", "`SELECT`");
				put("SET", "`SET`");
				put("SHOW", "`SHOW`");
				put("SHUTDOWN", "`SHUTDOWN`");
				put("SMALLINT", "`SMALLINT`");
				put("SONAME", "`SONAME`");
				put("SQL_BIG_TABLES", "`SQL_BIG_TABLES`");
				put("SQL_BIG_SELECTS", "`SQL_BIG_SELECTS`");
				put("SQL_LOW_PRIORITY_UPDATES", "`SQL_LOW_PRIORITY_UPDATES`");
				put("SQL_LOG_OFF", "`SQL_LOG_OFF`");
				put("SQL_LOG_UPDATE", "`SQL_LOG_UPDATE`");
				put("SQL_SELECT_LIMIT", "`SQL_SELECT_LIMIT`");
				put("SQL_SMALL_RESULT", "`SQL_SMALL_RESULT`");
				put("SQL_BIG_RESULT", "`SQL_BIG_RESULT`");
				put("SQL_WARNINGS", "`SQL_WARNINGS`");
				put("STRAIGHT_JOIN", "`STRAIGHT_JOIN`");
				put("STARTING", "`STARTING`");
				put("STATUS", "`STATUS`");
				put("STRING", "`STRING`");
				put("TABLE", "`TABLE`");
				put("TABLES", "`TABLES`");
				put("TEMPORARY", "`TEMPORARY`");
				put("TERMINATED", "`TERMINATED`");
				put("TEXT", "`TEXT`");
				put("THEN", "`THEN`");
				put("TIME", "`TIME`");
				put("TIMESTAMP", "`TIMESTAMP`");
				put("TINYBLOB", "`TINYBLOB`");
				put("TINYTEXT", "`TINYTEXT`");
				put("TINYINT", "`TINYINT`");
				put("TRAILING", "`TRAILING`");
				put("TO", "`TO`");
				put("TYPE", "`TYPE`");
				put("USE", "`USE`");
				put("USING", "`USING`");
				put("UNIQUE", "`UNIQUE`");
				put("UNLOCK", "`UNLOCK`");
				put("UNSIGNED", "`UNSIGNED`");
				put("UPDATE", "`UPDATE`");
				put("USAGE", "`USAGE`");
				put("USER", "`USER`");
				put("VALUES", "`VALUES`");
				put("VARCHAR", "`VARCHAR`");
				put("VARIABLES", "`VARIABLES`");
				put("VARYING", "`VARYING`");
				put("VARBINARY", "`VARBINARY`");
				put("WITH", "`WITH`");
				put("WRITE", "`WRITE`");
				put("WHEN", "`WHEN`");
				put("WHERE", "`WHERE`");
				put("YEAR", "`YEAR`");
				put("YEAR_MONTH", "`YEAR_MONTH`");
				put("ZEROFILL", "`ZEROFILL`");
			}
		};
	}
}
