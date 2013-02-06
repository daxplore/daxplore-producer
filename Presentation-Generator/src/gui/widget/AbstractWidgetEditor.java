package gui.widget;

import tools.NumberlineCoverage.NumberlineCoverageException;

@SuppressWarnings("serial")
public abstract class AbstractWidgetEditor<T> extends AbstractWidget<T> {
	
	public static class InvalidContentException extends Exception {

		public InvalidContentException(NumberlineCoverageException e) {
			super(e);
		}
	}
	
	public abstract T getContent() throws InvalidContentException;
	
}
