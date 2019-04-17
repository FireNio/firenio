/*
 * Copyright 2015 The FireNio Project
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
package test.others.json;

public class StringLexer {

    private String codes;

    private int index;

    public StringLexer(int index, String codes) {
        this.index = index;
        this.codes = codes;
    }

    public char charAt(int index) {
        return codes.charAt(index);
    }

    public boolean complate() {
        return index + 1 == codes.length();
    }

    //	public char [] sub(int start,int size){
    //		if(start+size < codes.length){
    //			char []chs = new char[size];
    //			for (int i = 0; i < size; i++) {
    //				chs[i] = 
    //			}
    //		}else{
    //			return null;
    //		}
    //	}

    public char current() {
        return codes.charAt(index);
    }

    public int currentIndex() {
        return index;
    }

    public boolean next() {
        return ++index < codes.length();
        //		if (++index == codes.length) {
        //			//throw new JSONSyntaxException("eof");
        //			return EOF;
        //		}
        //		return codes[index];
    }

    public boolean next(int size) {
        return (index += size) < codes.length();
    }

    public void previous() {
        index--;
    }

    @Override
    public String toString() {
        return new String(codes);
    }
}
