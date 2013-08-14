package org.daxplore.producer.tools;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * An extention of the Properties class where the properties are sorted
 * by key when written to file.
 * 
 * <p>Based on the How-To <a href="http://www.rgagnon.com/javadetails/java-0614.html">
 * Sort Properties when saving</a> using the terms given in the
 * <a href="http://www.rgagnon.com/varia/faq-e.htm#license">FAQ</a>: 
 * <i>"There is no restriction to use individual How-To in a development
 * (compiled/source) but a mention is appreciated."</i></p>
 */
public class SortedProperties extends Properties {
	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -8809785959474015736L;
	
	/**
     * Creates an empty property list with no default values.
     */
	public SortedProperties() {
		super();
	}
	
	/**
     * Creates an empty property list with the specified defaults.
     *
     * @param   defaults   the defaults.
     */
	public SortedProperties(Properties defaults) {
		super(defaults);
	}

	/**
	 * Overrides the keys method to return the keys in a sorted order.
	 * 
	 * <p>Called by the store method.</p>
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized Enumeration keys() {
		Enumeration keysEnum = super.keys();
		Vector keyList = new Vector(size());
		while (keysEnum.hasMoreElements()) {
			keyList.add(keysEnum.nextElement());
		}
		Collections.sort(keyList);
		return keyList.elements();
	}
}
