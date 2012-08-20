package tools;

public class Pair<K, V> implements Comparable<Pair<K, V>> {
	protected final K key;
	protected final V value;

	public Pair(K key, V value) throws NullPointerException {
		if (key == null || value == null) {
			throw new NullPointerException();
		}
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

	public int compareTo(Pair<K, V> otherPair) {
		if (null != otherPair) {
			if (otherPair.equals(this)) {
				return 0;
			} else if (otherPair.hashCode() > this.hashCode()) {
				return 1;
			} else if (otherPair.hashCode() < this.hashCode()) {
				return -1;
			}
		}
		return -1;
	}

	public boolean equals(Pair<K, V> otherPair) {
		return otherPair != null
				&& otherPair.key.equals(this.key)
				&& otherPair.value.equals(this.value);
	}

	public int hashCode() {
		return key.hashCode() + 31 * value.hashCode();
	}
}