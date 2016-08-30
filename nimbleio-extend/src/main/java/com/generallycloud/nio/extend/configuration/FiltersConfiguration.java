package com.generallycloud.nio.extend.configuration;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.component.Configuration;

public class FiltersConfiguration {

	private List<Configuration> filters = new ArrayList<Configuration>();

	public List<Configuration> getFilters() {
		return filters;
	}

	protected void addFilters(Configuration filter) {
		this.filters.add(filter);
	}
	
	
}
