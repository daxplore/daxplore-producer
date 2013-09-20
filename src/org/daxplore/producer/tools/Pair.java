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

	@Override
	public String toString() {
		return "Key: " + key + "\tValue: " + value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 23;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if ((key == null && other.key != null) || (key!=null  && !key.equals(other.key))) {
			return false;
		}
		if ((value == null && other.value != null) || (value!=null && !value.equals(other.value))) {
			return false;
		}
		return true;
	}

}
