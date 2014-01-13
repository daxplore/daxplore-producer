/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.cli;
/*package commandclient;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;

import commandclient.MetaCommand.Destinations;

public class EnumConverterFactory implements IStringConverterFactory {

	Class type;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<? extends IStringConverter<?>> getConverter(Class forType) {
		if(forType.isEnum()){
			type = forType;
			return EnumConverter.class;
		}
		return null;
	}

	public class EnumConverter<E> implements IStringConverter<enum E>{
		public E convert(String value){
			return type.valueOf(value);
		}
	}
}*/
