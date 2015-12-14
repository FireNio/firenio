package com.gifisan.mtp.component;


public class ServletConfig extends FilterConfig{

	public String getStringValue(String key){
		
		Object value = this.getAttribute(key);
		if (value == null) {
			return null;
		}
		return String.valueOf(value);
		
	}
}
