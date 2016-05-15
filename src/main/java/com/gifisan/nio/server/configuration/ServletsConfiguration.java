package com.gifisan.nio.server.configuration;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.component.Configuration;

public class ServletsConfiguration {
	
	private List<Configuration> servlets = new ArrayList<Configuration>();

	public List<Configuration> getServlets() {
		return servlets;
	}

	protected void addServlets(Configuration servlet) {
		this.servlets.add(servlet);
	}

}
