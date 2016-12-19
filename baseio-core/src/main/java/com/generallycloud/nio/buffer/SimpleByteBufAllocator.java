package com.generallycloud.nio.buffer;


public class SimpleByteBufAllocator extends AbstractByteBufAllocator {

	public SimpleByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		super(capacity, unitMemorySize, isDirect);
	}

	//FIXME 判断余下的是否足够，否则退出循环
	@Override
	protected PooledByteBuf allocate(ByteBufNew byteBufNew,int limit, int start, int end, int size) {

		ByteBufUnit[] units = this.units;

		int freeSize = 0;

		for (; start < end;) {

			ByteBufUnit unit = units[start];

			if (!unit.free) {

				start = unit.blockEnd;

				freeSize = 0;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.index + 1;
				start = blockEnd - size;

				ByteBufUnit unitStart = units[start];
				ByteBufUnit unitEnd = unit;

				unitStart.free = false;
				unitStart.blockEnd = blockEnd;
				unitEnd.free = false;

				mask = blockEnd;
				
				return byteBufNew.newByteBuf(this).produce(start, blockEnd, limit);
			}

			start++;
		}

		return null;
	}

	@Override
	protected void doRelease(ByteBufUnit buf) {

		ByteBufUnit memoryStart = buf;
		ByteBufUnit memoryEnd = units[memoryStart.blockEnd - 1];

		memoryStart.free = true;
		memoryEnd.free = true;
	}

}
