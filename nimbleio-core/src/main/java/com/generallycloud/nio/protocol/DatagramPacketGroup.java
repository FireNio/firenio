package com.generallycloud.nio.protocol;

public class DatagramPacketGroup {

	private DatagramPacket[]	packets	;
	private int			size		;

//	private Logger logger =LoggerFactory.getLogger(DatagramPacketGroup.class);
	
	public DatagramPacketGroup(int max) {
		this.packets = new DatagramPacket[max];
	}

	public void addDatagramPacket(DatagramPacket packet) {
		
		size++;
		packets[packet.getSequenceNo()] = packet;
		packet.getData();
		
//		logger.debug("_________________________add______packet:{}",packet);
		
	}

	public DatagramPacket[] getDatagramPackets() {

		return packets;
	}

	public int size() {
		return size;
	}
	
	public void foreach(DPForeach foreach){
		
		int size = this.size;
		
		for (int i = 0; i < size; i++) {
			
			DatagramPacket p = packets[i];
			
			if (p == null) {
				size++;
				continue;
			}
			
			foreach.onPacket(p);
		}
	}

	public interface DPForeach{
		
		void onPacket(DatagramPacket packet);
	}
	
}
