package com.generallycloud.test.nio.buffer;

import java.util.Random;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.SimplyByteBufAllocator;
import com.generallycloud.nio.common.ThreadUtil;

public class TestMarkByteBufAllocator {

	static ByteBufAllocator	allocator	= new SimplyByteBufAllocator(20, 5, false);

	public static void main(String[] args) throws Exception {

		allocator.start();

		Runnable r = () -> {
			for (;;) {

				int random = new Random().nextInt(40);

				if (random == 0) {
					continue;
				}

				ByteBuf buf = allocator.allocate(random);

				System.out.println(buf+Thread.currentThread().getName());
				
				buf.release();

				ThreadUtil.sleep(1000);
			}
		};

		ThreadUtil.execute(r);

		ThreadUtil.execute(r);

	}
}
