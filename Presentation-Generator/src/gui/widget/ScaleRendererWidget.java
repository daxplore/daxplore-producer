package gui.widget;

import java.awt.Dimension;

import javax.swing.JLabel;

import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

@SuppressWarnings("serial")
public class ScaleRendererWidget extends OurListWidget {
		JLabel textref = new JLabel();
		JLabel numberline = new JLabel();
		JLabel value = new JLabel();
		private Option option;
		
	public ScaleRendererWidget() {
		add(textref);
		add(numberline);
		add(new JLabel(" -> "));
		add(value);
	}
	
	public void setOption(MetaScale.Option option) {
		this.option = option;
		textref.setText(option.getTextRef().getRef());
		numberline.setText(option.getTransformation().toString());
		value.setText(Double.toString(option.getValue()));
	}
	
	public MetaScale.Option getOption() {
		return option;
	}
}
