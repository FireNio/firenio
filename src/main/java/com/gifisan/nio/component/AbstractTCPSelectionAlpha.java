package com.gifisan.nio.component;

public abstract class AbstractTCPSelectionAlpha implements TCPSelectionAlpha {

	protected EndPointWriter	endPointWriter;

	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public void setEndPointWriter(EndPointWriter endPointWriter) {
		this.endPointWriter = endPointWriter;
	}
}
