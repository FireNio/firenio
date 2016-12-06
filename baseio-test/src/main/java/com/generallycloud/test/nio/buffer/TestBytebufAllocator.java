package com.generallycloud.test.nio.buffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.SimpleByteBufAllocator;

public class TestBytebufAllocator {

	
	public static void main(String[] args) throws Exception {
		
		test();
		
	}
	
	static void test() throws Exception{
		
		ByteBufAllocator allocator = new SimpleByteBufAllocator(20, 1, false);
		
		allocator.start();
		
		ByteBuf buf = allocator.allocate(2);
		
		buf.reallocate(4);
		
		System.out.println(buf);
	}
	
	
}
