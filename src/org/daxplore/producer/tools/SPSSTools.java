package org.daxplore.producer.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SPSSTools {
	
	public static Set<String> getNonAsciiStrings(File spssFile, Charset charset) throws Exception {
		Set<String> stringSet = new HashSet<String>();
		Charset ascii = Charset.forName("US-ASCII");
		CharsetEncoder asciiEncoder = ascii.newEncoder();
		SPSSFile sf = null;
		SPSSFile sf2 = null;
		try {
			FileFormatInfo ffi = new FileFormatInfo();
			ffi.namesOnFirstLine = false;
			ffi.asciiFormat = ASCIIFormat.CSV;
			ffi.compatibility = Compatibility.GENERIC;
			sf = new SPSSFile(spssFile, ascii);
			sf.logFlag = false;
			sf.loadMetadata();
		} catch (SPSSFileException e) {
			sf.close();
			e.printStackTrace();
			throw new Exception("Couldn't open file");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Couldn't open file");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Couldn't open file");
		}
		
		try {
			sf2 = new SPSSFile(spssFile, charset);
			sf2.logFlag = false;
			sf2.loadMetadata();
		} catch (SPSSFileException | IOException e) {
			sf.close();
			if(sf2!=null) {
				sf2.close();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Couldn't open file");
		}
		
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
		sf.close();
		sf2.close();
		return stringSet;
	}
}
