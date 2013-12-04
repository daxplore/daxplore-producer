package org.daxplore.producer.gui.widget;

import org.daxplore.producer.tools.NumberlineCoverage.NumberlineCoverageException;

@SuppressWarnings("serial")
public interface AbstractWidgetEditor<T> extends AbstractWidget<T> {
	
	public static class InvalidContentException extends Exception {

		public InvalidContentException(NumberlineCoverageException e) {
			super(e);
		}
	}
	
	public T getContent() throws InvalidContentException;
	
}
