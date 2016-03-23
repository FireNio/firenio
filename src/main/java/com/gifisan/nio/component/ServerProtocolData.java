package com.gifisan.nio.component;

public class ServerProtocolData extends ProtocolDataImpl implements ProtocolData {

	private String		serviceName	= null;
	private boolean	beat			= false;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	protected boolean isBeat() {
		return beat;
	}

	protected void setBeat(boolean beat) {
		this.beat = beat;
	}

}