package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.Parameters;

public interface ParametersReadFuture extends ReadFuture{

	public abstract Parameters getParameters();
	
}
