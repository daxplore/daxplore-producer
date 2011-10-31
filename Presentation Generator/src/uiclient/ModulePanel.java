package uiclient;

import javax.swing.JPanel;

public abstract class ModulePanel extends JPanel {
	private static final long serialVersionUID = -4355981660649931234L;
	
	public abstract void save();
	public abstract void restore();
}
