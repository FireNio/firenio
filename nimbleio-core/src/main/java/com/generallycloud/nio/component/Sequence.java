package com.generallycloud.nio.component;

import com.generallycloud.nio.component.concurrent.FixedAtomicInteger;

public class Sequence {

	public FixedAtomicInteger	AUTO_CHANNEL_ID	= new FixedAtomicInteger(1, Integer.MAX_VALUE);

}
