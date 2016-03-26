package com.gifisan.nio.client;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.ProtocolDataImpl;
import com.gifisan.nio.component.Parameters;

public class Response extends ProtocolDataImpl {

	private Parameters	parameters	= null;

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}

}
