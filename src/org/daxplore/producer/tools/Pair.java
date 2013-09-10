package org.daxplore.producer.tools;

public class Pair<K, V> {
	protected final K key;
	protected final V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public String toString() {
		return "Key: " + key + "\tValue: " + value;
	}

	public boolean equals(Object otherPair) {
		if(otherPair instanceof Pair) {
			@SuppressWarnings("rawtypes")
			Pair op = (Pair)otherPair;
			return otherPair != null
					&& ((op.key == null && key == null) || (key != null && key.equals(op.key)))
					&& ((op.value == null && value == null) || (value != null && value.equals(op.value)));
		}
		return false;
	}
}
