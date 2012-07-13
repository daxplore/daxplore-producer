package tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Set;

import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.FileFormatInfo.ASCIIFormat;
import org.opendatafoundation.data.FileFormatInfo.Compatibility;
import org.opendatafoundation.data.spss.SPSSFile;
import org.opendatafoundation.data.spss.SPSSFileException;
import org.opendatafoundation.data.spss.SPSSVariable;

public class SPSSTools {
	
	public static Set<String> getNonAsciiStrings(File spssFile) throws Exception {
		Set<String> stringSet = new HashSet<String>();
		Charset charset = Charset.forName("US-ASCII");
		CharsetEncoder asciiEncoder = charset.newEncoder();
		SPSSFile sf = null;
		try {
			FileFormatInfo ffi = new FileFormatInfo();
			ffi.namesOnFirstLine = false;
			ffi.asciiFormat = ASCIIFormat.CSV;
			ffi.compatibility = Compatibility.GENERIC;
			sf = new SPSSFile(spssFile, charset);
			sf.logFlag = false;
			sf.loadMetadata();
			
		} catch (SPSSFileException e) {
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
		
		for(int i = 0; i < sf.getVariableCount(); i++){
			SPSSVariable var = sf.getVariable(i);
			String s = var.getShortName();
			if(!asciiEncoder.canEncode(s)){
				stringSet.add(s);
			}
			s = var.getName();
			if(!asciiEncoder.canEncode(s)){
				stringSet.add(s);
			}
			s = var.getLabel();
			if(!asciiEncoder.canEncode(s)){
				stringSet.add(s);
			}
			for(String s2: var.categoryMap.keySet()){
				if(!asciiEncoder.canEncode(s2)){
					stringSet.add(s2);
				}
			}
		}
		sf.close();
		return stringSet;
	}
}
