package test.others;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.FileUtil;
import com.gifisan.nio.common.PropertiesLoader;

public class Test {

	public static void main(String[] args) throws IOException {

		//http://www.daqianduan.com/4280.html
		
		PropertiesLoader.setBasepath("nio");

		InputStream stream = Test.class.getClassLoader().getResourceAsStream("aa");
		
		String content = FileUtil.input2String(stream, Encoding.UTF8);
		
		String [] ls = content.split("\n");
		
		for(String s: ls){
			
			String [] s1 = s.split("\t");
			
			String c = s1[0];
			String d = s1[1];
			
//			StringBuilder b = new StringBuilder();
			
//			C200(200,"OK","200 OK");
			
			System.out.println("/**");
			System.out.print(" *");
			System.out.println(s1[2].replace("\n", "").replace("\r", ""));
			System.out.println(" */");
			
			String r = "C"+c+"("+c+",\""+d+"\",\" "+c+" "+d+"\"),";
			
			System.out.println(r);
			
//			int i = s.indexOf("\t");
//			String s1 = s.substring(0,i);
//			String []s2 = s1.split("")
			
			
			
			
		}

	}
}
