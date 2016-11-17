package com.generallycloud.nio.buffer;


public class SimplyByteBufAllocator extends AbstractByteBufAllocator {

	public SimplyByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		super(capacity, unitMemorySize, isDirect);
	}

	protected ByteBuf allocate(int capacity, int begin, int end, int size) {

		ByteBufUnit[] units = this.units;
		
		for (; begin < end;) {
			
			ByteBufUnit unitBegin = units[begin];
			
			if (!unitBegin.free) {
				
				begin = unitBegin.blockEnd;
				
				continue;
			}
			
			int blockEnd = unitBegin.blockEnd;
			
			int blockBegin = unitBegin.blockBegin;
			
			int freeSize = blockEnd - blockBegin;
			
			if(freeSize < size){
				
				begin = blockEnd;
				
				continue;
			}
			
			ByteBufUnit unitEnd = units[blockEnd - 1];

			blockBegin = unitEnd.blockBegin;
			
			blockEnd = unitEnd.blockEnd;
			
			if (freeSize == size) {
				
				setBlock(unitBegin, unitEnd, false);
				
				mask = blockEnd;
				
				return bufFactory.newByteBuf(this).produce(blockBegin,blockEnd,capacity);
			}
			
			unitBegin = units[blockBegin];
			
			ByteBufUnit buf1 = units[blockBegin + size - 1];
			
			ByteBufUnit buf2 = units[buf1.index + 1];
			
			setBlock(buf2, unitEnd, true);
			
			setBlock(unitBegin, buf1, false);
			
			mask = buf2.index;
			
			return bufFactory.newByteBuf(this).produce(blockBegin,blockEnd,capacity);
		}
		
		return null;
	}
	
	protected void doStart() throws Exception {
		
		super.doStart();
		
		ByteBufUnit begin = units[0];
		ByteBufUnit end = units[capacity - 1];
		
		setBlock(begin, end, true);
	}
	
	private void setBlock(ByteBufUnit begin,ByteBufUnit end,boolean free){
		
		int beginIndex = begin.index;
		int endIndex = end.index + 1;
		
		begin.free = free;
		begin.blockBegin = beginIndex;
		begin.blockEnd = endIndex;
		
		end.free = free;
		end.blockBegin = beginIndex;
		end.blockEnd = endIndex;
		
		if (free) {
			logger.debug("free {}>{},,,,,{}",new Object[]{beginIndex,endIndex,Thread.currentThread().getName()});
		}else{
			logger.debug("allocate {}>{},,,,,{}",new Object[]{beginIndex,endIndex,Thread.currentThread().getName()});
		}
	}

	protected void doRelease(ByteBufUnit begin) {
		
		ByteBufUnit[] bufs = this.units;
		
		int beginIndex = begin.blockBegin;
		int endIndex = begin.blockEnd;
		
		ByteBufUnit memoryBegin = begin;
		ByteBufUnit memoryEnd = bufs[endIndex - 1];
		
		memoryBegin.free = true;
		memoryEnd.free = true;
		
		if (beginIndex != 0) {
			
			ByteBufUnit leftBuf = bufs[beginIndex - 1];
			
			if (leftBuf.free) {
				memoryBegin = bufs[leftBuf.blockBegin];
			}
		}
		
		if (endIndex != capacity) {
			
			ByteBufUnit rightBuf = bufs[endIndex];
			
			if (rightBuf.free) {
				memoryEnd = bufs[rightBuf.blockEnd - 1];
			}
		}
		
		setBlock(memoryBegin, memoryEnd, true);
	}

}
