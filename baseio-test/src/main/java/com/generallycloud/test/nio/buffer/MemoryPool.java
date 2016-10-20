package com.generallycloud.test.nio.buffer;

import java.util.concurrent.locks.ReentrantLock;

public class MemoryPool {

	private MemoryBlock memoryBlock;
	
	private MemoryUnit memoryUnitStart;
	
	private MemoryUnit memoryUnitEnd;
	
	private MemoryUnit [] memoryUnits;
	
	private ReentrantLock lock = new ReentrantLock();
	
	public MemoryBlock poll(int size){
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		try {
			
			MemoryBlock b = memoryBlock;
			
			for(;;){
				
				if (b == null) {
					return null;
				}
				
				if(b.getSize() < size){
					
					b = b.getNext();
					
					continue;
				}
				
				MemoryBlock r = new MemoryBlock();
				
				MemoryUnit start = b.getStart();
				
				MemoryUnit end = memoryUnits[start.getIndex() + size];
				
				r.setMemory(start, end);
				
				b.setMemory(end.getNext(), b.getEnd());
				
				return r.use();
			}
			
		} finally {
			lock.unlock();
		}
		
	}
	
	public void release(MemoryBlock memoryBlock){
		
		ReentrantLock lock = this.lock;
		
		lock.lock();
		
		try {
			
			memoryBlock.free();
			
			MemoryUnit start = memoryBlock.getStart();
			MemoryUnit end = memoryBlock.getEnd();
			
			MemoryUnit left = start.getPrevious();
			MemoryUnit right = end.getNext();
			
			if (left != null && !left.isUsing()) {
				if (right != null && !right.isUsing()) {
					
					MemoryBlock bLeft = memoryBlock.getPrevious();
					MemoryBlock bRight = memoryBlock.getNext();
					
					MemoryUnit newStart = bLeft.getStart();
					MemoryUnit newEnd = bRight.getEnd();
					
					bLeft.setMemory(newStart, newEnd);
					bLeft.setNext(bRight);
					
					bRight.setPrevious(bLeft);
					
				}else{
					
					MemoryBlock bLeft = memoryBlock.getPrevious();
					
					MemoryUnit newStart = bLeft.getStart();
					MemoryUnit newEnd = memoryBlock.getEnd();
					
					bLeft.setMemory(newStart, newEnd);
				}
			}else{
				
				if (right != null && !right.isUsing()) {
					
					MemoryBlock bRight = memoryBlock.getNext();
					
					MemoryUnit newStart = memoryBlock.getStart();
					MemoryUnit newEnd = bRight.getEnd();
					
					bRight.setMemory(newStart, newEnd);
					
				}else{
					
					MemoryBlock s = this.memoryBlock;
					
					int index = memoryBlock.getEnd().getIndex();
					
					for(;;){
						
						if (s.getEnd().getIndex() < index) {
							
							MemoryBlock next = s.getNext();
							
							if (next == null) {
								
								s.setNext(memoryBlock);
								
								return;
							}
							
							s = next;
							
							continue;
						}
						
						MemoryBlock bLeft = s.getPrevious();
						MemoryBlock bRight = s;
						
						memoryBlock.setPrevious(bLeft);
						bLeft.setNext(memoryBlock);
						
						memoryBlock.setNext(bRight);
						bRight.setPrevious(memoryBlock);
						
						return;
					}
				}
			}
			
		} finally {
			lock.unlock();
		}
	}
	
	public void initialize(int size){
		
		memoryUnits = new MemoryUnit[size];
		
		MemoryUnit next = memoryUnitStart;
		
		for (int i = 0; i < size; i++) {
			
			MemoryUnit temp = new MemoryUnit(i);
			
			memoryUnits[i] = temp;
			
			if (next == null) {
				
				next = temp;
				
				memoryUnitStart = temp;
				
				continue;
			}
			
			next.setNext(temp);
			
			temp.setPrevious(next);
			
			next = temp;
			
		}
		
		memoryUnitEnd = next;
		
		memoryBlock = new MemoryBlock();
		
		memoryBlock.setMemory(memoryUnitStart, memoryUnitEnd);
	}
	
}
