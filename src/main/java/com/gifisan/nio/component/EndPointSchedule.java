package com.gifisan.nio.component;

import java.io.IOException;

public interface EndPointSchedule {

	public abstract boolean schedule(EndPoint endPoint) throws IOException;

	public abstract ProtocolData getProtocolData();

}
