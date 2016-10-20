package com.generallycloud.nio.common;

public class StringLexer {

	private int index;
	
	private char [] codes;
	
	public StringLexer(int index, char[] codes) {
		this.index = index;
		this.codes = codes;
	}
	
	public void previous(){
		index--;
	}
	
	public boolean next(int size){
		return (index+=size) < codes.length;
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

	public boolean next(){
		return ++index < codes.length;
//		if (++index == codes.length) {
//			//throw new JSONSyntaxException("eof");
//			return EOF;
//		}
//		return codes[index];
	}
	
	public char charAt(int index){
		return codes[index];
	}
	
	public char current(){
		return codes[index];
	}
	
	public int currentIndex(){
		return index;
	}
	
	public boolean complate(){
		return index + 1 == codes.length;
	}
	
	public String toString() {
		return new String(codes);
	}
}
