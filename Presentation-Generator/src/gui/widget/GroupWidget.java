package gui.widget;

import java.util.Locale;
import javax.swing.JLabel;

import daxplorelib.metadata.MetaGroup;

public class GroupWidget extends OurListWidget {

	public GroupWidget(MetaGroup mg) {
		add(new JLabel(mg.getTextRef().get(new Locale("sv")))); //TODO: get universal locale
	}
	
}
