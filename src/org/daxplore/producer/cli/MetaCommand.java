/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.cli;

import java.io.File;

import org.daxplore.producer.daxplorelib.ImportExportManager.Formats;
import org.daxplore.producer.tools.MyTools;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Manage metadata")
public class MetaCommand {
	
	class FormatFile {
		public Formats format;
		public File file;
		public Integer version = null;
		
		public FormatFile(){}
		
		public FormatFile(Formats format, File file){
			this.format = format;
			this.file = file;
		}
	}
	
	class FormatFileConverter implements IStringConverter<FormatFile> {

		@Override
		public FormatFile convert(String value) {
			FormatFile ff = new FormatFile();
			String formatname = null;
			String filename = null;
			if(value.contains("=")){
				String[] s = value.split("=");
				formatname = s[0];
				filename = s[1];
			} else {
				formatname = value;
			}
			try {
				ff.format = Formats.valueOf(formatname.toUpperCase());
			} catch (IllegalArgumentException e){
				String formats = MyTools.join(Formats.values(), ", ");
				throw new ParameterException(formatname + "is not a valid option. Valid options are: " + formats);
			}
			switch (ff.format) {
				case RAW:
					if(filename != null){
						try{
							Integer version = Integer.parseInt(filename);
							ff.version = version;
						} catch (NumberFormatException e){
							throw new ParameterException("Numeric version expected for " + ff.format.toString());
						}
					}
					return ff;
				case DATABASE:
					if(filename != null) {
						throw new ParameterException("No file needed for " + ff.format.toString());
					}
					return ff;
				case JSON:
				case RESOURCE:
					if(filename != null) {
						ff.file = new File(filename);
						if(ff.file.isDirectory()){
							throw new ParameterException("File is a direcory");
						}
						return ff;
					}
					throw new ParameterException("Filename needed, write like: " + ff.format.toString() + "=filename");
				default:
					throw new ParameterException("How did I get here?");
			}
		}
		
	}
	
	@Parameter(names={"--src", "-s"}, description = "Where should the data go", converter= FormatFileConverter.class)
	FormatFile src = new FormatFile(Formats.DATABASE, null);
	
	@Parameter(names={"--dest","-d"}, description = "Where should the data go", converter= FormatFileConverter.class)
	FormatFile dest = new FormatFile(Formats.DATABASE, null);

	
	public void run(File file){
		/*MetaData metadata;
		try {
			DaxploreFile daxplorefile = new DaxploreFile(file, false);
			
			if(src.format == dest.format){
				System.out.println("Source and destination can't be the same");
			}
			
			switch (src.format) {
			case DATABASE:
				metadata = new MetaData(daxplorefile);
				break;
			case RAW:
				Integer version;
				if(src.version != null){
					version = src.version;
				} else {
				//	version = daxplorefile.getAbout().getActiveRawData();
				}
				//ImportedData imported = daxplorefile.getAbout().getImportedData(version);
				//metadata = new MetaData(imported, daxplorefile);
				break;
			case JSON:
			case RESOURCE:
				BufferedReader br;
				if(src.file.exists() && src.file.canRead()){
					try {
						br = new BufferedReader(new FileReader(src.file));
						//metadata = new MetaData(br, src.format, daxplorefile);
					} catch (FileNotFoundException e) {
						System.out.println("File " + src.file.getName() + " does not exist");
						return;
					}
				} else {
					if(!src.file.exists()){
						System.out.println("File " + src.file.getName() + " does not exist");
					} else {
						System.out.println("File " + src.file.getName() + " can't be read");
					}
					return;
				}
				break;
			default:
				System.out.println("I shouldn't end up here");
				return;
			}
			switch (dest.format){
			case DATABASE:
				//metadata.save();
				break;
			case RAW:
				System.out.println("Destination can't be RAW");
				return;
			case RESOURCE:
			case JSON:
				if(dest.file.canWrite()){
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(dest.file));
						//metadata.export(bw, dest.format);
						bw.flush();
					} catch (IOException e) {
						System.out.println("Can't write to " + dest.file.getName());
						return;
					}
				} else {
					System.out.println("Can't write to " + dest.file.getName());
					return;
				}
			}
		} catch (DaxploreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
