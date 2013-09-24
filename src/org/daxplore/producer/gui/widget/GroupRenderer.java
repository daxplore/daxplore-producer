package org.daxplore.producer.gui.widget;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.daxplore.producer.daxplorelib.metadata.MetaGroup;
import org.daxplore.producer.gui.Settings;

@SuppressWarnings("serial")
public class GroupRenderer extends AbstractWidget<MetaGroup> {
	
	private MetaGroup metaGroup;
	
	private JLabel label;
	
	public GroupRenderer() {
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label);
	}
	

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		label.setText(metaGroup.getTextRef().get(Settings.getDefaultLocale()));
	}
	
}
