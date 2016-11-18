package com.generallycloud.test.nio.buffer;

import java.util.Random;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.SimpleByteBufAllocator;
import com.generallycloud.nio.buffer.SimplyByteBufAllocator;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ThreadUtil;

public class TestSimplyByteBufAllocator {

	static ByteBufAllocator	allocator	= null;

	public static void main(String[] args) throws Exception {

		int capacity = 100;
		int unit = 1;
		int max = unit * capacity / 5;

		allocator = new SimplyByteBufAllocator(capacity, unit, false);
//		 allocator = new SimpleByteBufAllocator(capacity, unit, false);

		allocator.start();

		Runnable r = () -> {
			for (;;) {

				int random = new Random().nextInt(max);

				if (random == 0) {
					continue;
				}

				ByteBuf buf = allocator.allocate(random);

				if (buf == null) {
					System.out.println(buf + Thread.currentThread().getName());
				}

				ThreadUtil.sleep(new Random().nextInt(20));

				ReleaseUtil.release(buf);
				
//				String des = allocator.toString();
//				
//				if (des.indexOf("free=100") == -1) {
//					System.out.println();
//				}

				// ThreadUtil.sleep(10);
				int i = 0;
				i++;
			}
		};

		ThreadUtil.execute(r);

		 ThreadUtil.execute(r);
		
//		 ThreadUtil.execute(r);
//		
//		 ThreadUtil.execute(r);

	}
}
