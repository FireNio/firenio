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
package com.generallycloud.test.nio.balance;

/**
 * @author wangkai
 *
 */
public class TestNode<T> {

	private T machine;
	
	// debug for hit rate
	private T last;

	public T getMachine() {
		return machine;
	}

	public void setMachine(T machine) {
		this.machine = machine;
	}
	
	public T getLast() {
		return last;
	}

	public void setLast(T last) {
		this.last = last;
	}
	
	public boolean compare(){
		return machine == last;
	}

	@Override
	public String toString() {
		if (machine == null) {
			return "-1";
		}
		return machine.toString();
	}
	
}
