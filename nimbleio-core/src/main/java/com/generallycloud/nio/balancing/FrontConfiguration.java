package com.generallycloud.nio.balancing;

public class FrontConfiguration {

	private int FRONT_FACADE_PORT;
	
	private int FRONT_REVERSE_PORT;
	
	private boolean isAcceptBeat;

	public int getFRONT_FACADE_PORT() {
		return FRONT_FACADE_PORT;
	}

	public void setFRONT_FACADE_PORT(int fRONT_FACADE_PORT) {
		FRONT_FACADE_PORT = fRONT_FACADE_PORT;
	}

	public int getFRONT_REVERSE_PORT() {
		return FRONT_REVERSE_PORT;
	}

	public void setFRONT_REVERSE_PORT(int fRONT_REVERSE_PORT) {
		FRONT_REVERSE_PORT = fRONT_REVERSE_PORT;
	}

	public boolean isAcceptBeat() {
		return isAcceptBeat;
	}

	public void setAcceptBeat(boolean isAcceptBeat) {
		this.isAcceptBeat = isAcceptBeat;
	}
	
}
