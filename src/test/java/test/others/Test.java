package test.others;

import java.math.BigDecimal;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.PropertiesLoader;

public class Test {

	public static void main(String[] args) {

		PropertiesLoader.setBasepath("nio");

		int allTime = 2560000;

		long spend = 36457;

		BigDecimal b = new BigDecimal(allTime * 1000L).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP);

		System.out.println(b);

	}
}
