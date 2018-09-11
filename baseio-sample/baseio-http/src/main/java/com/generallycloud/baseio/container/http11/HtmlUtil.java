/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.container.http11;

public class HtmlUtil {

    public static final String HTML_HEADER;

    public static final String HTML_POWER_BY;

    public static final String HTML_BOTTOM;

    static {

        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>\n");
        builder.append("<html lang=\"en\">\n");
        builder.append("	<head>\n");
        builder.append(
                "		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n");
        builder.append(
                "		<meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=3, minimum-scale=1, user-scalable=no\">\n");
        builder.append("		<title>baseio</title>\n");
        builder.append("		<style type=\"text/css\"> \n");
        builder.append("			p {margin:15px;}\n");
        builder.append("			a:link { color:#F94F4F;  }\n");
        builder.append("			a:visited { color:#F94F4F; }\n");
        builder.append("			a:hover { color:#000000; }\n");
        builder.append("		</style>\n");
        builder.append("	</head>\n");
        builder.append("	<body style=\"font-family:Georgia;\">\n");
        HTML_HEADER = builder.toString();

        builder = new StringBuilder();
        builder.append("\t\t<hr>\n");
        builder.append("\t\t<p style=\"color: #FDA58C\">\n");
        builder.append("\t\t	Powered by baseio@\n");
        builder.append(
                "\t\t	<a style=\"color:#F94F4F;\" href=\"https://github.com/generallycloud/baseio#readme\">\n");
        builder.append("\t\t		https://github.com/generallycloud/baseio\n");
        builder.append("\t\t	</a>\n");
        builder.append("\t\t</p>\n");
        HTML_POWER_BY = builder.toString();

        builder = new StringBuilder();
        builder.append("	</body>\n");
        builder.append("</html>");

        HTML_BOTTOM = builder.toString();
    }

}
