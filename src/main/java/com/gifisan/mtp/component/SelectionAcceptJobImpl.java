package com.gifisan.mtp.component;

import java.nio.channels.SelectionKey;

import com.gifisan.mtp.schedule.SelectionAcceptJob;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class SelectionAcceptJobImpl implements SelectionAcceptJob{
	
	private SelectionAccept accept = null;
	
	private SelectionKey selectionKey = null;
	
	public SelectionAcceptJobImpl(SelectionAccept accept,SelectionKey selectionKey) {
		this.accept = accept;
		this.selectionKey = selectionKey;
	}

	public void schedule() {
		try {
			this.accept(selectionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void accept(SelectionKey selectionKey) throws Exception {
		accept.accept(selectionKey);
	}


}
