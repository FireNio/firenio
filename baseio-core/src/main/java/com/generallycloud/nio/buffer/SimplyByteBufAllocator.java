package com.generallycloud.nio.buffer;


public class SimplyByteBufAllocator extends AbstractByteBufAllocator {

	public SimplyByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		super(capacity, unitMemorySize, isDirect);
	}
	
	private String tName(){
		return Thread.currentThread().getName();
	}

	@Override
	protected PooledByteBuf allocate(ByteBufNew byteBufNew,int limit, int begin, int end, int size) {

		logger.debug("申请内存____________________________{},{}",size,tName());
		
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
				
				logger.debug("申请内存结束____________________________{},{},{}",new Object[]{unitBegin.index,unitEnd.index,tName()});
				
				setBlock(unitBegin, unitEnd, false);
				
				mask = blockEnd;
				
				return byteBufNew.newByteBuf(this).produce(blockBegin,blockEnd,limit);
			}
			
			unitBegin = units[blockBegin];
			
			ByteBufUnit buf1 = units[blockBegin + size - 1];
			
			ByteBufUnit buf2 = units[buf1.index + 1];
			
			setBlock(buf2, unitEnd, true);
			
			logger.debug("申请内存前释放____________________________{},{},{}",new Object[]{ buf2.index,unitEnd.index,tName()});
			
			setBlock(unitBegin, buf1, false);
			
			logger.debug("申请内存结束____________________________{},{},{}",new Object[]{unitBegin.index,buf1.index,tName()});
			
			System.out.println();
			
			mask = buf2.index;
			
			return byteBufNew.newByteBuf(this).produce(blockBegin,blockEnd,limit);
		}
		
		return null;
	}
	
	@Override
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
		
//		if (free) {
//			logger.debug("free {}>{},,,,,{}",new Object[]{beginIndex,endIndex,tName()});
//		}else{
//			logger.debug("allocate {}>{},,,,,{}",new Object[]{beginIndex,endIndex,tName()});
//		}
	}

	@Override
	protected void doRelease(ByteBufUnit begin) {
		
		ByteBufUnit[] bufs = this.units;
		
		int beginIndex = begin.blockBegin;
		int endIndex = begin.blockEnd;
		
		ByteBufUnit bufBegin = begin;
		ByteBufUnit bufEnd = bufs[endIndex - 1];
		
		bufBegin.free = true;
		bufEnd.free = true;
		
		if (beginIndex != 0) {
			
			ByteBufUnit leftBuf = bufs[beginIndex - 1];
			
			if (leftBuf.free) {
				bufBegin = bufs[leftBuf.blockBegin];
			}
		}
		
		if (endIndex != capacity) {
			
			ByteBufUnit rightBuf = bufs[endIndex];
			
			if (rightBuf.free) {
				bufEnd = bufs[rightBuf.blockEnd - 1];
			}
		}
		
		setBlock(bufBegin, bufEnd, true);
		
		logger.debug("释放内存____________________________{},{},{}",new Object[]{bufBegin.index,bufEnd.index,tName()});
		System.out.println();
	}

}
