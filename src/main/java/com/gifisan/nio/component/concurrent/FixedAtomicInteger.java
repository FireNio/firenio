package com.gifisan.nio.component.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedAtomicInteger {

	private AtomicInteger	atomiticInteger;

	private int			max_value;

	private int			init_value;

	public FixedAtomicInteger(int init, int max) {
		this.init_value = init;
		this.max_value = max;
		this.atomiticInteger = new AtomicInteger(init);
	}

	public FixedAtomicInteger(int max) {
		this(0, max);
	}

	public FixedAtomicInteger() {
		this(0, Integer.MAX_VALUE);
	}

	public final int getAndIncrement() {
		
		for (;;) {
			
			int current = atomiticInteger.get();
			
			int next;
			
			if (current == max_value) {
				next = init_value;
			}else{
				next = current + 1;
			}
			
			if (atomiticInteger.compareAndSet(current, next))
				return current;
		}
	}

	public final int getAndDecrement() {
		
		for (;;) {
			
			int current = atomiticInteger.get();
			
			int next;
			
			if (current == init_value) {
				next = max_value;
			}else{
				next = current - 1;
			}
			
			if (atomiticInteger.compareAndSet(current, next))
				return current;
		}
	}

	public final int incrementAndGet() {
		
		for (;;) {
			int current = atomiticInteger.get();
			
			int next;
			
			if (current == max_value) {
				next = init_value;
			}else{
				next = current + 1;
			}
			
			if (atomiticInteger.compareAndSet(current, next))
				return next;
		}
	}

	public final int decrementAndGet() {
		
		for (;;) {
			
			int current = atomiticInteger.get();
			
			int next;
			
			if (current == init_value) {
				next = max_value;
			}else{
				next = current - 1;
			}
			
			if (atomiticInteger.compareAndSet(current, next))
				return next;
		}
	}

}
