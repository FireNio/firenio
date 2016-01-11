package test;

import java.io.IOException;
import java.util.Random;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestClearSession {
	
	public static void main(String[] args) throws IOException {
		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		String param = ClientUtil.getParamString();
		for (int i = 0; i < 5000; i++) {
			NIOClient client = new NIOClient("localhost", 8300, getSessionID());
			client.connect();
			client.request(serviceKey, param);
			client.close();
			System.out.println(i);
		}
	}
	
	private static String [] sessionIDs = new String [] {
			"yyyy1","yyyy2","yyyy3","yyyy4","yyyy5","yyyy6","yyyy7","yyyy8"
			,"yyyy21","yyyy22","yyyy23","yyyy24","yyyy25","yyyy26","yyyy27","yyyy28"
			,"yyyy31","yyyy32","yyyy33","yyyy34","yyyy35","yyyy36","yyyy37","yyyy38"
			,"yyyy41","yyyy42","yyyy43","yyyy44","yyyy45","yyyy46","yyyy47","yyyy48"
			,"yyyy51","yyyy52","yyyy53","yyyy54","yyyy55","yyyy56","yyyy57","yyyy58"};
	
	private static Random random = new Random();
	
	private static String getSessionID(){
		
		return sessionIDs[random.nextInt(sessionIDs.length)];
		
	}
	
	
	
	public static void main1(String[] args) {
		
		
		
		System.out.println(getSessionID());
		
		
	}
}
