package daxplorelib.metadata.textreference;

class TextReferenceReference implements Comparable<TextReferenceReference> {

	protected String reference;

	public TextReferenceReference(String reference) {
		this.reference = reference;
	}
	
	public String getRef() {
		return reference;
	}
	
	@Override
	public int compareTo(TextReferenceReference o) {
		return reference.compareTo(o.reference);
	}
}
