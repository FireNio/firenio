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
package com.generallycloud.baseio.codec.protobuf;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.codec.protobase.ProtobaseFrame;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public class ProtobufUtil {

    private Map<String, Parser<? extends MessageLite>> parses = new HashMap<>();

    public void regist(String name, Parser<? extends MessageLite> parser) {
        parses.put(name, parser);
    }

    public void regist(MessageLite messageLite) {
        parses.put(messageLite.getClass().getName(), messageLite.getParserForType());
    }

    public Parser<? extends MessageLite> getParser(String name)
            throws InvalidProtocolBufferException {
        Parser<? extends MessageLite> parser = parses.get(name);
        if (parser == null) {
            throw new InvalidProtocolBufferException("did not found parse by name " + name);
        }
        return parser;
    }

    public MessageLite getMessage(ProtobaseFrame frame) throws InvalidProtocolBufferException {
        Parser<? extends MessageLite> parser = getParser(frame.getReadText());
        return parser.parseFrom(frame.getReadBinary());
    }

    public void writeProtobuf(MessageLite messageLite, ProtobaseFrame frame)
            throws InvalidProtocolBufferException {
        writeProtobuf(messageLite.getClass().getName(), messageLite, frame);
    }

    public void writeProtobuf(String parserName, MessageLite messageLite, ProtobaseFrame frame)
            throws InvalidProtocolBufferException {
        frame.write(parserName.getBytes());
        // FIXME 判断array是否过大
        byte[] array = messageLite.toByteArray();
        frame.writeBinary(array, 0, array.length);
    }

}
