package com.generallycloud.nio.codec.http2.future;


public enum Http2FrameType {

	FRAME_TYPE_DATA(0x0), 
	FRAME_TYPE_HEADERS(0x1), 
	FRAME_TYPE_PRIORITY(0x2), 
	FRAME_TYPE_RST_STREAM(0x3), 
	FRAME_TYPE_SETTINGS(0x4), 
	FRAME_TYPE_PUSH_PROMISE(0x5), 
	FRAME_TYPE_PING(0x6), 
	FRAME_TYPE_GOAWAY(0x7), 
	FRAME_TYPE_WINDOW_UPDATE(0x8), 
	FRAME_TYPE_CONTINUATION(0x9),
	
	FRAME_TYPE_PREFACE(0x10),
	FRAME_TYPE_FRAME_HEADER(0x11);
	
	private int value;
	
	private byte byteValue;

	private Http2FrameType(int value) {
		this.value = value;
		this.byteValue = (byte) value;
	}

	public int getValue() {
		return value;
	}
	
	public byte getByteValue() {
		return byteValue;
	}

	private static Http2FrameType _getValue(int i){
		
		Http2FrameType [] values = Http2FrameType.values();
		
		for(Http2FrameType v : values){
			
			if (v.getValue() == i) {
				return v;
			}
		}
		
		return null;
	}
	
	public static Http2FrameType getValue(int i){
		return VALUES[i];
	}
	
	private static Http2FrameType[] VALUES;
	
	static{
		
		VALUES = new Http2FrameType[values().length];
		
		for (int i = 0; i < VALUES.length; i++) {
			VALUES[i] = _getValue(i);
		}
	}
	
}
