package com.generallycloud.nio.common;

public class HtmlUtil {

	public static final String HTML_HEADER;
	
	public static final String HTML_BOTTOM;
	
	static{
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("<!DOCTYPE html>\n");
		builder.append("<html lang=\"en\">\n");
		builder.append("	<head>\n");
		builder.append("		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
		builder.append("		<meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=no\">\n");
		builder.append("		<title>nimbleio</title>\n");
		builder.append("		<style type=\"text/css\"> \n");
		builder.append("			p {margin:15px;}\n");
		builder.append("			a:link { color:#F94F4F;  }");
		builder.append("			a:visited { color:#F94F4F; }");
		builder.append("			a:hover { color:#000000; }");
		builder.append("		</style>\n");
		builder.append("	</head>\n");
		builder.append("	<body style=\"font-family:Georgia;\">\n");
		HTML_HEADER = builder.toString();
		
		builder = new StringBuilder();
		builder.append("	</body>\n");
		builder.append("</html>");
		
		HTML_BOTTOM = builder.toString();
	}
	
}
