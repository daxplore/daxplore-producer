/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.cli;

import java.util.Locale;

import com.beust.jcommander.IStringConverter;


public class LocaleConverter implements IStringConverter<Locale>{

	@Override
	public Locale convert(String value) {
		return new Locale(value);
	}

}
