package test.others;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.MD5Token;


public class Test {

	public static void main(String[] args) {

		String test ="test123";
		
		byte [] array = test.getBytes();
		
		StringBuilder b = new StringBuilder();
		
		for (int i = 0; i < array.length; i++) {
			b.append((char)array[i]);
		}
		
		System.out.println(b);
		
		System.out.println(MD5Token.getInstance().getLongToken("wk", Encoding.UTF8));

	}
}
