package org.daxplore.producer.gui.resources;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconResources {
	
	private static Map<String, ImageIcon> iconMap = new HashMap<>();
	
	public static ImageIcon get(String name) throws FileNotFoundException {
		if(iconMap.containsKey(name)) {
			ImageIcon icon = iconMap.get(name);
			if(icon != null) {
				return icon;
			} else {
				throw new FileNotFoundException("No such file: " + name);
			}
		} else {
			URL url = IconResources.class.getResource(name);
			if(url == null) {
				iconMap.put(name, null);
				throw new FileNotFoundException("No such file: "+ name); 
			}
			ImageIcon icon = new ImageIcon(url);
			iconMap.put(name, icon);
			return icon;
		}
	}
}
