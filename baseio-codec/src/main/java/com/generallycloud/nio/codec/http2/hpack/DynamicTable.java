package com.generallycloud.nio.codec.http2.hpack;

import static com.generallycloud.nio.codec.http2.hpack.Http2CodecUtil.MAX_HEADER_TABLE_SIZE;
import static com.generallycloud.nio.codec.http2.hpack.Http2CodecUtil.MIN_HEADER_TABLE_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.codec.http2.future.Http2Header;

final class DynamicTable {

	// a circular queue of header fields
	private List<Http2Header>	headers	= new ArrayList<Http2Header>();
	int						head;
	int						tail;
	private long				size;
	private long				capacity		= -1;						

	/**
	 * Creates a new dynamic table with the specified initial capacity.
	 */
	DynamicTable(long initialCapacity) {
		setCapacity(initialCapacity);
	}

	/**
	 * Return the number of header fields in the dynamic table.
	 */
	public int length() {
		return headers.size();
	}

	/**
	 * Return the current size of the dynamic table. This is the sum of the
	 * size of the entries.
	 */
	public long size() {
		return size;
	}

	/**
	 * Return the maximum allowable size of the dynamic table.
	 */
	public long capacity() {
		return capacity;
	}

	/**
	 * Return the header field at the given index. The first and newest entry
	 * is always at index 1, and the oldest entry is at the index length().
	 */
	public Http2Header getEntry(int index) {
		if (index <= 0 || index > length()) {
			throw new IndexOutOfBoundsException();
		}
		int i = head - index;
		if (i < 0) {
			return headers.get(i + headers.size());
		} else {
			return headers.get(index);
		}
	}

	/**
	 * Add the header field to the dynamic table. Entries are evicted from the
	 * dynamic table until the size of the table and the new header field is
	 * less than or equal to the table's capacity. If the size of the new entry
	 * is larger than the table's capacity, the dynamic table will be cleared.
	 */
	public void add(Http2Header header) {
		int headerSize = header.size();
		if (headerSize > capacity) {
			clear();
			return;
		}
		while (capacity - size < headerSize) {
			remove();
		}
		headers.add(header);
		size += header.size();
	}

	/**
	 * Remove and return the oldest header field from the dynamic table.
	 */
	public Http2Header remove() {
		return null;
	}

	/**
	 * Remove all entries from the dynamic table.
	 */
	public void clear() {
		head = 0;
		tail = 0;
		size = 0;
	}

	/**
	 * Set the maximum size of the dynamic table. Entries are evicted from the
	 * dynamic table until the size of the table is less than or equal to the
	 * maximum size.
	 */
	public void setCapacity(long capacity) {
		if (capacity < MIN_HEADER_TABLE_SIZE || capacity > MAX_HEADER_TABLE_SIZE) {
			throw new IllegalArgumentException("capacity is invalid: " + capacity);
		}
		// initially capacity will be -1 so init won't return here
		if (this.capacity == capacity) {
			return;
		}
		this.capacity = capacity;

		if (capacity == 0) {
			clear();
		} else {
			// initially size will be 0 so remove won't be called
			while (size > capacity) {
				remove();
			}
		}

		
	}
}
