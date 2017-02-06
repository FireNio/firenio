/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.test.test;

import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.common.DebugUtil;

public abstract class ITestThread implements Runnable {

	private CountDownLatch	latch;

	private int			time;

	private long			last_time;

	protected void setTime(int time) {
		this.time = time;
		this.latch = new CountDownLatch(time);
	}

	public int getTime() {
		return time;
	}

	public void await() throws InterruptedException {
		latch.await();
	}

	public abstract void prepare() throws Exception;

	public abstract void stop();

	public void addCount() {

		latch.countDown();

		long c = latch.getCount();

		if (c % 40000 == 0) {

			long now = System.currentTimeMillis();

			long passed = 0;

			if (last_time == 0) {
				last_time = now;
			} else {
				passed = now - last_time;
				last_time = now;
			}

			DebugUtil.debug("__________________________" + c + "\t" + "___" + passed);
		}

	}

}
