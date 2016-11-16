package com.generallycloud.nio.buffer;



public class MarkByteBufAllocator extends AbstractByteBufAllocator {

	public MarkByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		super(capacity, unitMemorySize, isDirect);
	}

	protected ByteBuf allocate(int capacity, int start, int end, int size) {

		PooledByteBuf[] bufs = this.bufs;
		
		for (; start < end;) {
			
			PooledByteBuf unitBegin = bufs[start];
			
			if (!unitBegin.isFree()) {
				
				start = unitBegin.getBlockEnd();
				
				continue;
			}
			
			int blockEnd = unitBegin.getBlockEnd();
			
			int freeSize = blockEnd - unitBegin.getBlockBegin();
			
			if(freeSize < size){
				
				start = blockEnd;
				
				continue;
			}
			
			PooledByteBuf unitEnd = bufs[blockEnd - 1];
			
			if (freeSize == size) {
				
				setBlock(unitBegin, unitEnd, false);
				
				mask = blockEnd;
				
				return unitBegin.produce(capacity);
			}
			
			PooledByteBuf buf1 = bufs[unitBegin.getIndex() + size - 1];
			
			PooledByteBuf buf2 = bufs[buf1.getIndex() + 1];
			
			setBlock(buf2, unitEnd, true);
			
			setBlock(unitBegin, buf1, false);
			
			mask = buf2.getIndex();
			
			return unitBegin.produce(capacity);
		}
		
		return null;
	}
	
	protected void doStart() throws Exception {
		
		super.doStart();
		
		PooledByteBuf begin = bufs[0];
		PooledByteBuf end = bufs[capacity - 1];
		
		setBlock(begin, end, true);
	}
	
	private void setBlock(PooledByteBuf begin,PooledByteBuf end,boolean free){
		
		int beginIndex = begin.getIndex();
		int endIndex = end.getIndex() + 1;
		
		begin.setFree(free);
		begin.setBlockBegin(beginIndex);
		begin.setBlockEnd(endIndex);
		
		end.setFree(free);
		end.setBlockBegin(beginIndex);
		end.setBlockEnd(endIndex);
	}

	protected void doRelease(PooledByteBuf buf) {
		
		PooledByteBuf[] bufs = this.bufs;

		int beginIndex = buf.getBlockBegin();
		int endIndex = buf.getBlockEnd();
		
		PooledByteBuf memoryBegin = buf;
		PooledByteBuf memoryEnd = bufs[endIndex - 1];
		
		memoryBegin.setFree(true);
		memoryEnd.setFree(true);
		
		if (beginIndex != 0) {
			
			PooledByteBuf leftBuf = bufs[beginIndex - 1];
			
			if (leftBuf.isFree()) {
				memoryBegin = bufs[leftBuf.getBlockBegin()];
			}
		}
		
		if (endIndex != capacity) {
			
			PooledByteBuf rightBuf = bufs[endIndex];
			
			if (rightBuf.isFree()) {
				memoryEnd = bufs[rightBuf.getBlockEnd() - 1];
			}
		}
		
		setBlock(memoryBegin, memoryEnd, true);
	}

}
