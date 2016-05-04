package com.gifisan.nio.jms.server;

import java.util.List;

import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class P2PConsumerQueue implements ConsumerQueue{

	private LinkedList<Consumer>	consumers	= new LinkedListM2O<Consumer>(128);

	//FIXME flush失败后如何处理，写入queue不太合适
	public Consumer poll(long timeout) {
		
		Consumer consumer = consumers.poll(timeout);
		
		if (consumer == null) {
			return null;
		}
		
		//FIXME offer when pushed
//		offer(consumer);
		
		return consumer.clone();
	}

	public int size(){
		return consumers.size();
	}

	public void offer(Consumer consumer) {
		if(!consumers.offer(consumer)){
			//FIXME offer failed
		}
	}

	public void remove(Consumer consumer) {
		
	}

	public Consumer[] snapshot() {
		return null;
	}

	public void remove(List<Consumer> consumers) {
		
	}
	
	
	
}