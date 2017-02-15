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
package com.generallycloud.nio.container;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.AbstractLifeCycle;

public class Sequence extends AbstractLifeCycle {

	public AtomicInteger	AUTO_ROOM_ID;

	public AtomicInteger	AUTO_PLUGIN_INDEX;

	@Override
	protected void doStart() throws Exception {

		AUTO_ROOM_ID = new AtomicInteger();

		AUTO_PLUGIN_INDEX = new AtomicInteger();
	}

	@Override
	protected void doStop() throws Exception {

		AUTO_ROOM_ID = null;

		AUTO_PLUGIN_INDEX = null;
	}

}
