package test.others;


public class Test {

	public static void main(String[] args) {

		String test ="test123";
		
		byte [] array = test.getBytes();
		
		StringBuilder b = new StringBuilder();
		
		for (int i = 0; i < array.length; i++) {
			b.append((char)array[i]);
		}
		
		System.out.println(b);

	}
}
