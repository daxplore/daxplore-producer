package ui;

import javax.swing.Icon;

public class Module {
	public ModulePanel panel;
	public Icon pressed, unpressed;
	public String name;
	
	public Module(ModulePanel panel, Icon unpressed, Icon pressed, String name){
		this.name = name;
		this.panel = panel;
		this.unpressed = unpressed;
		this.pressed = pressed;
	}
}
