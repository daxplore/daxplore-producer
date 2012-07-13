package cli;

import java.util.Locale;

import com.beust.jcommander.IStringConverter;


public class LocaleConverter implements IStringConverter<Locale>{

	@Override
	public Locale convert(String value) {
		return new Locale(value);
	}

}
