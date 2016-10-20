package com.generallycloud.nio.component;

import java.io.IOException;

public interface Connectable {

	public abstract Session connect() throws IOException;
	
}
