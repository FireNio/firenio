package com.generallycloud.nio.common.database;

import java.util.Map;

public abstract class AbstractQuery implements DataBaseQuery {

	protected Map<String, String> sys_fields = null;
	
	protected AbstractQuery (){
		setSys_fields();
	}
	
	public String getColumnName(String columnName) {
		String key = columnName.toUpperCase();
		if (sys_fields.containsKey(key)) {
			return sys_fields.get(key);
		} else {
			return columnName;
		}
	}
	
	abstract void setSys_fields();;
	
	
	
}
