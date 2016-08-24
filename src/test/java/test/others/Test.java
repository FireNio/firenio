package test.others;

import java.io.IOException;
import java.util.HashMap;

import com.gifisan.nio.common.MathUtil;

public class Test {

	public static void main(String[] args) throws IOException {

		int j = 1;
		for (int i = 0; i < 8; i++) {
			j <<= 1;
			System.out.println(j);
		}

		System.out.println("==================");
		
		for (int i = 4; i < 8; i++) {
			
			System.out.println(MathUtil.byte2BinaryString((byte)i));
			int y = i & 3;
			System.out.println(y);
			
			
		}
		
		HashMap map = new HashMap(16);
		
		
		map.put("k1", "v1");
		
	}
}
