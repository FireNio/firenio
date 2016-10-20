package com.generallycloud.nio.common;

import java.util.ArrayList;
import java.util.List;

//关键字：前缀，后缀，部分匹配表
public class KMPByteUtil {

	private byte[]	match_array;

	private int[]		match_table;

	public KMPByteUtil(byte[] match_array) {
		this.initialize(match_array);
	}

	private void initialize(byte[] match_array) {
		this.match_array = match_array;
		this.match_table = new int[match_array.length];
		this.initialize_part_match_table();
	}

	private void initialize_part_match_table() {

		int length = this.match_array.length;

		// 直接从两位开始比较
		for (int i = 2; i < length; i++) {

			match_table[i] = initialize_part_match_table0(match_array, i);
		}
	}

	private int initialize_part_match_table0(byte[] array, int length) {

		int e = 0;

		WORD: for (int i = 1; i < length; i++) {

			int t = 0;
			int p = 0;
			int s = length - i;

			for (int j = 0; j < i; j++) {
				if (array[p++] != array[s++]) {
					continue WORD;
				}
				t++;
			}
			e = t;
		}
		return e;
	}

	public int match(byte[] source_array) {
		return match(source_array, 0,source_array.length);
	}

	public int match(byte[] source_array, int begin,int end) {

		if (source_array == null || begin < 0 || end > source_array.length) {
			return -1;
		}

		if (source_array.length - begin < this.match_array.length) {
			return -1;
		}
		
		if (begin + match_array.length > end) {
			return -1;
		}

		int source_length = end;

		int index = begin;

		int match_length = this.match_array.length;

		byte[] match_array = this.match_array;

		int[] match_table = this.match_table;

		LOOP: for (; index < source_length;) {

			for (int i = 0; i < match_length; i++) {

				if (source_array[index + i] != match_array[i]) {

					if (i == 0) {
						index++;
					} else {
						index += (i - match_table[i]);
					}
					continue LOOP;
				}
			}

			return index;
		}

		return -1;
	}

	public List<Integer> match_all(byte[] source_array) {

		if (source_array == null) {
			return null;
		}

		if (source_array.length < match_array.length) {
			return null;
		}

		List<Integer> matchs = new ArrayList<Integer>();

		if (source_array.equals(match_array)) {
			matchs.add(0);
			return matchs;
		}

		int source_length = source_array.length;

		int index = 0;

		int match_length = this.match_array.length;

		byte[] match_array = this.match_array;

		int[] match_table = this.match_table;

		LOOP: for (; index < source_length;) {

			if (source_length - index < match_length) {

				break;
			}

			for (int i = 0; i < match_length; i++) {

				if (source_array[index + i] != match_array[i]) {

					if (i == 0) {
						index++;
					} else {
						index += (i - match_table[i]);
					}
					continue LOOP;
				}
			}

			matchs.add(index);
			index += match_length;
		}
		return matchs;
	}

	public static void main(String[] args) {

		String s1 = "1111111111111111111211111111112111121111211111111";

		String match = "1112";

		KMPByteUtil kmp = new KMPByteUtil(match.getBytes());

		System.out.println(kmp.match_all(s1.getBytes()));
	}
}
