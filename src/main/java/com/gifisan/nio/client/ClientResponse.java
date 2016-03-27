package com.gifisan.nio.client;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ProtocolDataImpl;


public class ClientResponse extends ProtocolDataImpl {

	private Parameters	parameters	= null;

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}

}
