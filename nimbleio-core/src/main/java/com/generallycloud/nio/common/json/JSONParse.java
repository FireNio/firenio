package com.generallycloud.nio.common.json;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.test.ITest;
import com.generallycloud.nio.common.test.ITestHandle;

public class JSONParse {

	private static final Logger logger = LoggerFactory.getLogger(JSONParse.class);
	
	public static void main(String[] args) throws JSONSyntaxException {

		String text = "{\"a\":\"sss\",\"b\":true,\"c\": false ,\"1     d\":-3.6  ,\"e\":{\"a\":[true,false,1.]},\"f\":{\"a\":\"xx\"}}";
		System.out.println(JSON.stringToMap(text));
		System.out.println(com.alibaba.fastjson.JSONObject.parseObject(text));
		// System.out.println(text);
		// System.out.println(JSON.stringToMap(text));
		text = "[null,\"a\",-1.6,true,false,[1,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":-3.6,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[1,true,{\"a\":\"aa\"}]}}]]]";
		text = "[   null    ,   {  a   :   \"a\"   ,   b   :   1.   }   ,   1   ]";
		System.out.println(JSON.stringToArray(text));
//		testMyJson();
		testFastJson();
		System.out.println("1\f11");

	}

	static void testFastJson() {
		ITestHandle.doTest(new ITest() {

			public void test() {
				com.alibaba.fastjson.JSONArray
						.parseArray("[\"a\",true,true,false,[true,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":true,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[true,true,{\"a\":\"aa\"}]}}]]");
			}

		}, 500000, "Fast Json");
	}

	static void testMyJson() {
		ITestHandle.doTest(new ITest() {
			public void test() {
				try {
					JSON.stringToArray("[\"a\",true,true,false,[true,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":true,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[true,true,{\"a\":\"aa\"}]}}]]");
				} catch (JSONSyntaxException e) {
					logger.debug(e);
				}
			}
		}, 500000, "My Json");
	}

}
