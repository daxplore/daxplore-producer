/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.daxplorelib.metadata.textreference;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.RandomAccess;

import com.scottlogic.util.SortedList;

@SuppressWarnings("serial") 
public class TextTree extends SortedList<TextReferenceReference> implements RandomAccess {
		
	static class TextComparator implements Comparator<TextReferenceReference> {
		@Override
		public int compare(TextReferenceReference o1, TextReferenceReference o2) {
			return o1.compareTo(o2);
		}
	}
	
	static TextTree.TextComparator comparator = new TextComparator();
	
	public TextTree() {
		super(comparator);
	}
	
	public TextReference get(String textref) {
		Node n = findFirstNodeWithValue(new TextReferenceReference(textref));
		return n!=null? (TextReference)n.getValue(): null;
	}
	
	@Override
	public TextReference get(int index) {
		return (TextReference)super.get(index);
	}
	
	public TextReference remove(String textref) {
		Node n = findFirstNodeWithValue(new TextReferenceReference(textref));
		TextReference tr = null;
		if(n != null) {
			tr = (TextReference)(n.getValue());
			remove(n);
		}
		return tr;
	}
	
	public int indexOf(TextReference tr) { //TODO: implement properly as a tree search
		return Collections.binarySearch(this, tr);
	}
	
	public boolean add(TextReference tr) {
		Node n = findFirstNodeWithValue(tr);
		if(n != null) {
			return false;
		}
		return super.add(tr);
	}
	
	public Iterable<TextReference> iterable() {
		return new Iterable<TextReference>() {
			@Override
			public Iterator<TextReference> iterator() {
				return new Iterator<TextReference>() {
					private Iterator<TextReferenceReference> iter = TextTree.this.iterator();
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public TextReference next() {
						return (TextReference)iter.next();
					}

					@Override
					public void remove() {
						iter.remove();
					}
				};
			}
		};
	}
}
