package test.others;

public class Test1 {

static int size;
	
	public static void main(String[] args) {

//		String ss = "abcdefghijklmnopqrstu"; // 21位
		
		String ss = "abcde";

		StringBuilder builder = new StringBuilder(ss);

		assembly(builder, 3);
		
		System.out.println("assembly:"+size);
		
//		for (int i = 'a'; i < 'a' + 21; i++) {
//			System.out.print((char)i);
//		}
		
	}
	
	static void assembly(StringBuilder src , int length){
		
		int remain = src.length() - length;
		
		for (int i = 0; i < remain; i++) {
			
			int start = i;
			
			int end = length + i + 1;
			
			String s = src.substring(start, end);
			
			StringBuilder b2 = new StringBuilder(s);
			
			int len = b2.length();
			
			int j = (i % 2 + 1) % 2;
			
			for (; j < len; j++) {
				
				StringBuilder b2_copy = new StringBuilder(b2);
				
				b2_copy.deleteCharAt(j);
				
				assembly(null, b2_copy);
			}
		}
	}

	static void assembly(StringBuilder appender, StringBuilder remaining) {
		
		for (int i = 0; i < remaining.length(); i++) {
			
			StringBuilder _appender;
			
			if (appender == null) {
				
				_appender = new StringBuilder();
				
			}else{
				
				_appender = new StringBuilder(appender);
			}
			
			StringBuilder _remaining = new StringBuilder(remaining.toString());
			
			char c = _remaining.charAt(i);
			
			_appender.append(c);
			
			_remaining.deleteCharAt(i);
			
			if (_remaining.length() > 0) {
				
				assembly(_appender, _remaining);
				
			}else{
				
				System.out.println(_appender);
				
				size++;
				
//				if (size % 1000000 == 0) {
//					System.out.println(size);
//				}
			}
		}
	}
}
