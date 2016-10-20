package com.generallycloud.test.nio.others.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.common.StringUtil;

//关键字：前缀，后缀，部分匹配表
public class KMPUtil {

	private char[]	match_array;

	private int[]		match_table;

	private String		match_value;

	public KMPUtil(String value) {
		this.initialize(value);
	}

	private void initialize(String value) {
		if (StringUtil.isNullOrBlank(value)) {
			throw new IllegalArgumentException("null value");
		}
		this.match_value = value;
		this.match_array = match_value.toCharArray();
		this.match_table = new int[match_value.length()];
		this.initialize_part_match_table();
	}

	private void initialize_part_match_table() {

		int length = this.match_value.length();

		// 直接从两位开始比较
		for (int i = 2; i < length; i++) {

			match_table[i] = initialize_part_match_table0(match_array, i);
		}
	}

	private int initialize_part_match_table0(char[] array, int length) {

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

	public int match(String value) {
		return match(value, 0);
	}

	public int match(String value, int begin) {

		if (StringUtil.isNullOrBlank(value) || begin < 0) {
			return -1;
		}

		if (value.length() - begin < this.match_array.length) {
			return -1;
		}

		char[] source_array = value.toCharArray();

		int source_length = value.length();

		int index = begin;

		int match_length = this.match_array.length;

		char[] match_array = this.match_array;

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

	public List<Integer> match_all(String value) {

		if (StringUtil.isNullOrBlank(value)) {
			return null;
		}

		if (value.length() < match_value.length()) {
			return null;
		}

		List<Integer> matchs = new ArrayList<Integer>();

		if (value.equals(match_value)) {
			matchs.add(0);
			return matchs;
		}

		char[] source_array = value.toCharArray();

		int source_length = value.length();

		int index = 0;

		int match_length = this.match_array.length;

		char[] match_array = this.match_array;

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

		KMPUtil kmp = new KMPUtil(match);

		System.out.println(kmp.match_all(s1));

	}
}
