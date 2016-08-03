package com.gifisan.nio.connector;

import java.io.Closeable;

import com.gifisan.nio.component.Connectable;
import com.gifisan.nio.component.IOService;
import com.gifisan.nio.component.Session;

public interface IOConnector extends IOService, Connectable, Closeable {

	public abstract Session getSession();
}
