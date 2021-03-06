/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.daxplore.producer.daxplorelib.DaxploreException;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SPSSTools {
	
	public static Set<String> getNonAsciiStrings(File spssFile, Charset charset) throws DaxploreException {
		Set<String> stringSet = new HashSet<>();
		Charset ascii = Charset.forName("US-ASCII");
		CharsetEncoder asciiEncoder = ascii.newEncoder();
		FileFormatInfo ffi = new FileFormatInfo();
		ffi.namesOnFirstLine = false;
		ffi.asciiFormat = ASCIIFormat.CSV;
		ffi.compatibility = Compatibility.GENERIC;
		try (SPSSFile sf = new SPSSFile(spssFile, ascii);
				SPSSFile sf2 = new SPSSFile(spssFile, charset)) {
			sf.logFlag = false;
			sf.loadMetadata();
		
			sf2.logFlag = false;
			sf2.loadMetadata();
		
			for(int i = 0; i < sf.getVariableCount(); i++){
				SPSSVariable var = sf.getVariable(i);
				SPSSVariable var2 = sf2.getVariable(i);
				String s = var.getName();
				if(!asciiEncoder.canEncode(s)){
					stringSet.add(var2.getName());
				}
				s = var.getLabel();
				if(s!=null && !asciiEncoder.canEncode(s)){ //TODO figure out why this can be null
					stringSet.add(var2.getLabel());
				}
				Iterator<String> iter = var.categoryMap.keySet().iterator();
				Iterator<String> iter2 = var.categoryMap.keySet().iterator();
				
				while(iter.hasNext()) {
					String ss = iter.next();
					String ss2 = iter2.next();
					if(!asciiEncoder.canEncode(ss)){
						stringSet.add(ss2);
					}
				}
			}
		} catch (SPSSFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DaxploreException("Couldn't open file");
		}
		return stringSet;
	}
}
