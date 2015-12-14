package com.gifisan.mtp.component;

import java.nio.channels.SelectionKey;

import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class SelectionAcceptJobImpl implements SelectionAcceptJob{
	
	private SelectionAccept accept = null;
	
	private SelectionKey selectionKey = null;
	
	public SelectionAcceptJobImpl(SelectionAccept accept,SelectionKey selectionKey) {
		this.accept = accept;
		this.selectionKey = selectionKey;
	}

	public void run() {
		try {
			this.doJob();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			InnerEndPoint endPoint = (InnerEndPoint) selectionKey.attachment(); 
			endPoint.setAccepting(false);
		}
	}
	
	public void accept(SelectionKey selectionKey) throws Exception {
		accept.accept(selectionKey);
	}

	public void doJob() throws Exception {
		this.accept(selectionKey);
	}

}
