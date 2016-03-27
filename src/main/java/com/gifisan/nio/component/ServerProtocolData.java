package com.gifisan.nio.component;

public class ServerProtocolData extends ProtocolDataImpl implements ProtocolData {

	private boolean	beat			= false;

	protected boolean isBeat() {
		return beat;
	}

	protected void setBeat(boolean beat) {
		this.beat = beat;
	}

}