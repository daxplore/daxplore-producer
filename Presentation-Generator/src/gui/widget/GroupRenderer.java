package gui.widget;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import daxplorelib.metadata.MetaGroup;

@SuppressWarnings("serial")
public class GroupRenderer extends AbstractWidget<MetaGroup> {
	
	public MetaGroup metaGroup;
	
	private JLabel label;
	
	public GroupRenderer(MetaGroup mg) {
		this();
		setContent(mg);
	}
	
	public GroupRenderer() {
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		add(label);
	}
	

	@Override
	public void setContent(MetaGroup value) {
		this.metaGroup = value;
		label.setText(metaGroup.getTextRef().get(new Locale("sv")));
	}
	
}
