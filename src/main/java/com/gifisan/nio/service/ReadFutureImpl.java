package com.gifisan.nio.service;

import java.io.OutputStream;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.ReadFuture;

public abstract class ReadFutureImpl extends FutureImpl implements ReadFuture{
	
	private Parameters			parameters	= null;
	protected OutputStream		outputStream	= null;
	protected IOExceptionHandle	handle		= null;
	
	public ReadFutureImpl(String serviceName) {
		this.serviceName = serviceName;
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}
	
	public OutputStream getOutputStream(){
		return outputStream;
	}

	public void setIOEvent(OutputStream outputStream, IOExceptionHandle handle) {
		this.outputStream = outputStream;
		this.handle = handle;
	}

	public boolean hasOutputStream() {
		return outputStream != null;
	}
}
