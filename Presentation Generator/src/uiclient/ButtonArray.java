package uiclient;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class ButtonArray extends JPanel {
	private static final long serialVersionUID = 4830441575477716942L;
	private ButtonGroup group;
	private final ButtonObservable buttonObservable = new ButtonObservable();
	
	public ButtonArray(List<Module> modules){
		super(new GridLayout(modules.size(), 1), true);
		
		group = new ButtonGroup();

		ItemListener il = new ButtonItemListener();

		for(Module m: modules){
			ArrayButton b = new ArrayButton(m);
			b.addItemListener(il);
			group.add(b);
			add(b);
		}		
	}
	
	public void addObserver(Observer observer){
		buttonObservable.addObserver(observer);
	}
	
	
	
	private class ButtonObservable extends Observable {
		public void fire(Module m){
			setChanged();
			notifyObservers(m);
		}
	}
	
	private class ButtonItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent ex) {
			ArrayButton button = (ArrayButton) ex.getItemSelectable();
			String item = button.getActionCommand();
			boolean selected = (ex.getStateChange() == ItemEvent.SELECTED);
			System.out.println((selected?"Selected ":"Unselected ") + item);
			if(selected){
				buttonObservable.fire(button.module);
			}
		}
	}
	
	private class ArrayButton extends JToggleButton {
		private static final long serialVersionUID = 4858679672125110746L;
		public Module module;
		
		public ArrayButton(Module m){
			module = m;
			setIcon(m.unpressed);
			setSelectedIcon(m.pressed);
			setActionCommand(m.name);
		}
	}
}
