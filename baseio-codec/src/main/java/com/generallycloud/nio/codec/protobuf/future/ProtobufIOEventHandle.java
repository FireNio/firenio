package com.generallycloud.nio.codec.protobuf.future;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;

public abstract class ProtobufIOEventHandle extends IOEventHandleAdaptor {

	private Map<String, Parser<? extends MessageLite>>	parses	= new HashMap<String, Parser<? extends MessageLite>>();

	public void regist(String name, Parser<? extends MessageLite> parser) {
		parses.put(name, parser);
	}

	public void regist(MessageLite messageLite) {

		parses.put(messageLite.getClass().getName(), messageLite.getParserForType());
	}

	public Parser<? extends MessageLite> getParser(String name) throws InvalidProtocolBufferException {

		Parser<? extends MessageLite> parser = parses.get(name);

		if (parser == null) {
			throw new InvalidProtocolBufferException("did not found parse by name " + name);
		}

		return parser;
	}

}
