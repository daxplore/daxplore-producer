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