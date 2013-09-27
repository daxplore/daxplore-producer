package org.daxplore.producer.daxplorelib.metadata.textreference;

class TextReferenceReference implements Comparable<TextReferenceReference> {

	String reference;

	public TextReferenceReference(String reference) {
		this.reference = reference;
	}
	
	public String getRef() {
		return reference;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(TextReferenceReference o) {
		return reference.compareTo(o.reference);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj || obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TextReferenceReference other = (TextReferenceReference) obj;
		if (reference == null) {
			if (other.reference != null) {
				return false;
			}
		} else if (!reference.equals(other.reference)) {
			return false;
		}
		return true;
	}
}
