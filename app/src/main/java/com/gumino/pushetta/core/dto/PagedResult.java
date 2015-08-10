package com.gumino.pushetta.core.dto;


public class PagedResult<T> {
	int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	T previous;

	public T getPrevious() {
		return previous;
	}

	public void setPrevious(T previous) {
		this.previous = previous;
	}

	T next;

	public T getNext() {
		return next;
	}

	public void setNext(T next) {
		this.next = next;
	}

	int start_index;

	public int getStart_index() {
		return start_index;
	}

	public void setStart_index(int start_index) {
		this.start_index = start_index;
	}

	int end_index;

	public int getEnd_index() {
		return end_index;
	}

	public void setEnd_index(int end_index) {
		this.end_index = end_index;
	}

	int num_pages;

	public int getNum_pages() {
		return num_pages;
	}

	public void setNum_pages(int num_pages) {
		this.num_pages = num_pages;
	}

	T[] results;

	public T[] getResults() {
		return results;
	}

	public void setResults(T[] results) {
		this.results = results;
	}
}
