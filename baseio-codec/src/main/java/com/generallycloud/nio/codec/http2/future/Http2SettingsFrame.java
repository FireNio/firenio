package com.generallycloud.nio.codec.http2.future;


public interface Http2SettingsFrame extends Http2Frame {

	public static final int	SETTINGS_HEADER_TABLE_SIZE			= 0x1;
	public static final int	SETTINGS_ENABLE_PUSH				= 0x2;
	public static final int	SETTINGS_MAX_CONCURRENT_STREAMS		= 0x3;
	public static final int	SETTINGS_INITIAL_WINDOW_SIZE		= 0x4;
	public static final int	SETTINGS_MAX_FRAME_SIZE			= 0x5;
	public static final int	SETTINGS_MAX_HEADER_LIST_SIZE		= 0x6;
	
	public abstract int[] getSettings() ;

}
