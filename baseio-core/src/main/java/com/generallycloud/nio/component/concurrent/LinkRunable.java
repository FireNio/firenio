package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.AbstractLinkable;
import com.generallycloud.nio.Linkable;

public abstract class LinkRunable extends AbstractLinkable<Runnable> implements Runnable, Linkable<Runnable> {

	public LinkRunable(Runnable value) {
		super(value);
	}

}
