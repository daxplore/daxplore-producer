package gui.question;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataListener;

import daxplorelib.metadata.MetaScale;
import daxplorelib.metadata.MetaScale.Option;

@SuppressWarnings("serial")
public class ScaleListModel extends DefaultListModel<MetaScale.Option> {

	private MetaScale scale;

	public ScaleListModel(MetaScale scale) {
		this.scale = scale;
	}
	
	@Override
	public int getSize() {
		return scale.getOptions().size();
	}

	@Override
	public Option getElementAt(int index) {
		return scale.getOptions().get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

}
