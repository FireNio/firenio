package com.gifisan.mtp.common;



public class DebugUtil {

	
	public static void main(String[] args) {
		Exception e = new Exception("999999");
		e.printStackTrace();
		String msg = exception2string(e);
		System.out.println(msg);
	}
	
	public static String exception2string(Exception exception){
		StackTraceElement[] es = exception.getStackTrace();
		StringBuilder builder = new StringBuilder();
		builder.append(exception.toString());
		for (StackTraceElement e :es) {
			builder.append("\n\tat ");
			builder.append(e.toString());
		}
		return builder.toString();
		
	}
}
