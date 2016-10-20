package com.generallycloud.nio.protocol;

public class DatagramPacketFactory {

	private Calculagraph	calculagraph	;

	public DatagramPacketFactory(int markInterval, long currentMark) {
		this.calculagraph = new Calculagraph(markInterval, currentMark);
	}
	
	public DatagramPacketFactory(int markInterval) {
		this.calculagraph = new Calculagraph(markInterval);
	}

	public DatagramPacket createDatagramPacket(byte[] data) {

		return new DatagramPacket(calculagraph.getTimestamp(), calculagraph.getSequenceNO(),data);
	}
	
	public Calculagraph getCalculagraph(){
		return calculagraph;
	}

}
