package test.others;

import java.util.concurrent.locks.LockSupport;

import sun.misc.Unsafe;

public class TestLockSupport {
	
	 private static final Unsafe unsafe = Unsafe.getUnsafe();

	
	public static void main(String[] args) {
		
		
		final Thread t1 = new Thread(new Runnable() {
			
			public void run() {
				
				System.out.println("lock....");
				
				Thread t = Thread.currentThread();
				
				LockSupport.park();
				
			}
		});
		
		final Thread t2 = new Thread(new Runnable() {
			
			public void run() {
				
				System.out.println("unlock....");
				
				LockSupport.unpark(t1);
				
			}
		});
		
		t1.start();
		t2.start();
		
		
		
	}
	
	
	
	
	
}
