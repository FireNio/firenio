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
package com.generallycloud.test.io.protobuf;

import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest;
import com.generallycloud.test.io.protobuf.TestProtoBufBean.SearchRequest.Corpus;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class Test {

    static final int i = 0b01111111111111111111111111111111;

    public static void main(String[] args) throws InvalidProtocolBufferException {

        ByteString byteString = ByteString.copyFrom("222".getBytes());

        SearchRequest request = SearchRequest.newBuilder().setCorpus(Corpus.IMAGES)
                .setPageNumber(100).setQuery("test").setQueryBytes(byteString).setResultPerPage(-1)
                .build();

        byte[] data = request.toByteArray();

        SearchRequest r2 = SearchRequest.parseFrom(data);

        System.out.println(r2.toString());

        int i = 0b011111111111111111111111;

        System.out.println(i);
    }
}
