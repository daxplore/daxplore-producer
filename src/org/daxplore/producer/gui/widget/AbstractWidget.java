package org.daxplore.producer.gui.widget;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractWidget<T> extends JPanel {

	public abstract void setContent(T value);
	
}
