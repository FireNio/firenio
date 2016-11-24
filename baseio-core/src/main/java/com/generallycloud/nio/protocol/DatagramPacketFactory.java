package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;

public class DatagramPacketFactory {

	private Calculagraph	calculagraph	;
	
	private BaseContext 	context;
	
	public DatagramPacketFactory(BaseContext context,int markInterval, long currentMark) {
		this.context = context;
		this.calculagraph = new Calculagraph(markInterval, currentMark);
	}
	
	public DatagramPacketFactory(BaseContext context,int markInterval) {
		this.context = context;
		this.calculagraph = new Calculagraph(markInterval);
	}

	public DatagramPacket createDatagramPacket(byte[] data) {
		return new DatagramPacket(context,calculagraph.getTimestamp(), calculagraph.getSequenceNO(),data);
	}
	
	public Calculagraph getCalculagraph(){
		return calculagraph;
	}

}
