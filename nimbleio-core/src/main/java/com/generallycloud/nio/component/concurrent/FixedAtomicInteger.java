package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedAtomicInteger{

	private AtomicInteger	atomiticInteger;

	private int			max_value;

	private int			min_value;

	public FixedAtomicInteger(int min, int max) {
		this.min_value = min;
		this.max_value = max;
		this.atomiticInteger = new AtomicInteger(min);
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
				next = min_value;
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
			
			if (current == min_value) {
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
				next = min_value;
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
			
			if (current == min_value) {
				next = max_value;
			}else{
				next = current - 1;
			}
			
			if (atomiticInteger.compareAndSet(current, next))
				return next;
		}
	}
	
	public boolean compareAndSet(int expect,int update){
		return atomiticInteger.compareAndSet(expect, update);
	}
	
	public int getMaxValue(){
		return max_value;
	}
	
	public int getMinValue(){
		return min_value;
	}
}
