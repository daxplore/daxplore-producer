/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
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
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
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
	
	@Override
	public String toString() {
		return reference;
	}
}
