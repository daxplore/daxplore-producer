/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
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
@SuppressWarnings("serial")
public class SortedProperties extends Properties {
	
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
