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

	public boolean equals(Pair<K, V> otherPair) {
		return otherPair != null
				&& ((otherPair.key == null && key == null) || otherPair.key.equals(this.key))
				&& ((otherPair.value == null && value == null) || otherPair.value.equals(this.value));
	}
}