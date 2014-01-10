/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.gui.resources;

import java.awt.Color;

public class Colors {
	
	public static final Color listBackgroundEven = Color.WHITE;
	public static final Color listBackgroundOdd = new Color(242, 242, 242);
	public static final Color listSelectedEven = new Color(200, 200, 255);
	public static final Color listSelectedOdd = new Color(190, 190, 255);
	public static final Color listSelectedMouseEven = new Color(165, 165, 255);
	public static final Color listSelectedMouseOdd = listSelectedMouseEven;
	public static final Color listMouseEven = new Color(215, 215, 215);
	public static final Color listMouseOdd = listMouseEven;
	public static final Color listFocusBorder = new Color(100, 100, 200);
	public static final Color transparent = new Color(0,0,0,0);
	
	public static Color getRowColor(boolean isSelected, boolean mouseOver, boolean evenRow) {
		if(isSelected && mouseOver && evenRow) {
			return listSelectedMouseEven;
		}
		if(isSelected && mouseOver && !evenRow) {
			return listSelectedMouseOdd;
		}
		if(isSelected && !mouseOver && evenRow) {
			return listSelectedEven;
		}
		if(isSelected && !mouseOver && !evenRow) {
			return listSelectedOdd;
		}
		if(!isSelected && mouseOver && evenRow) {
			return listMouseEven;
		}
		if(!isSelected && mouseOver && !evenRow) {
			return listMouseOdd;
		}
		if(!isSelected && !mouseOver && evenRow) {
			return listBackgroundEven;
		}
		if(!isSelected && !mouseOver && !evenRow) {
			return listBackgroundOdd;
		}
		
		throw new AssertionError("More than 8 alternatives from 3 booleans");
	}
	
}
