package com.generallycloud.nio.buffer;


public class SimpleByteBufAllocator extends AbstractByteBufAllocator {

	public SimpleByteBufAllocator(int capacity, int unitMemorySize, boolean isDirect) {
		super(capacity, unitMemorySize, isDirect);
	}

	protected ByteBuf allocate(int capacity, int start, int end, int size) {

		PooledByteBuf[] memoryUnits = this.bufs;

		int freeSize = 0;

		for (; start < end;) {

			PooledByteBuf unit = memoryUnits[start];

			if (!unit.isFree()) {

				start = unit.getBlockEnd();

				freeSize = 0;

				continue;
			}

			if (++freeSize == size) {

				int blockEnd = unit.getIndex() + 1;
				start = blockEnd - size;

				PooledByteBuf memoryStart = memoryUnits[start];
				PooledByteBuf memoryEnd = unit;

				memoryStart.setFree(false);
				memoryStart.setBlockEnd(blockEnd);
				memoryEnd.setFree(false);

				mask = blockEnd;

				return memoryStart.produce(capacity);
			}

			start++;
		}

		return null;
	}

	protected void doRelease(PooledByteBuf buf) {

		PooledByteBuf memoryStart = buf;
		PooledByteBuf memoryEnd = bufs[memoryStart.getBlockEnd() - 1];

		memoryStart.setFree(true);
		memoryEnd.setFree(true);
	}

}
