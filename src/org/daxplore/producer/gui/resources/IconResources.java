package org.daxplore.producer.gui.resources;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconResources {
	
	private static Map<String, ImageIcon> iconMap = new HashMap<>();
	
	public static ImageIcon getIcon(String name) {
		if(iconMap.containsKey(name)) {
			return iconMap.get(name);
		} 
		
		URL url = IconResources.class.getResource("images/" + name);
		if(url == null) {
			iconMap.put(name, null);
			return null; 
		}
		ImageIcon icon = new ImageIcon(url);
		iconMap.put(name, icon);
		return icon;
	}
	
	public static Image getImage(String name) {
		ImageIcon icon = getIcon(name);
		if(icon == null) {
			return null;
		}
		return icon.getImage();
	}
}
