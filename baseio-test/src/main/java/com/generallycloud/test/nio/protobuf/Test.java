package com.generallycloud.test.nio.protobuf;

import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest.Corpus;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class Test {

	public static void main(String[] args) throws InvalidProtocolBufferException {
		
		ByteString byteString = ByteString.copyFrom("222".getBytes());
		
		SearchRequest request = SearchRequest
				.newBuilder()
				.setCorpus(Corpus.IMAGES)
				.setPageNumber(100)
				.setQuery("test")
				.setQueryBytes(byteString)
				.setResultPerPage(-1)
				.build();
		
		byte[] data= request.toByteArray();
		
		SearchRequest r2 = SearchRequest.parseFrom(data);
		
		System.out.println(r2.toString());
		
		
	}
}
