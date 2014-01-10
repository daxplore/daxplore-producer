/*******************************************************************************
 * Copyright (c) 2013-2014 Daniel Dunér, Axel Winkler.
 * All rights reserved. This program is free software: it is made
 * available under the terms of the GNU Public License v2.0 (or later)
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package org.daxplore.producer.tools;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.SortedMap;

import com.google.common.base.Charsets;
 
public class CharsetTest {
 
	public static void main(String[] args) {
		SortedMap<String, Charset> a = Charset.availableCharsets();
		for (String name : a.keySet()) {
			Charset c = a.get(name);
			if(!c.isRegistered()) {
				System.out.print("(x) ");
			}
			if (!Charset.isSupported(name) || !c.canEncode()) {
				System.out.print(name + ": not supported\n");
				continue;
			}
			System.out.print(name + ": ");
			ByteBuffer bb;
			try {
				bb = c.encode("a z");
			} catch(UnsupportedOperationException e) {
				System.out.print("not supported\n");
				continue;
			}
			int s = 0;
			byte b;
			try {
				while (true) {
					b = bb.get();
					System.out.printf("0x%X ", b);
					s++;
				}
			} catch (BufferUnderflowException e) {
				System.out.print(" -end- ");
			}
			System.out.print("(" + s + " bytes)");
			System.out.print(charset8bitTest(c)? " 8": " !8");
			System.out.print(charsetSPSSTest(c)? " s": " !s");
			System.out.print("\n");
		}
	}
	
	public static boolean charset8bitTest(Charset charset) {
		if(!charset.isRegistered() || !charset.canEncode()) {
			return false;
		}
		ByteBuffer bb;
		try {
			bb = charset.encode("a z");
		} catch(UnsupportedOperationException e) {
			return false;
		}
		int s = 0;
		try {
			byte b = 0;
			while (true) {
				byte b2;
				b2 = bb.get();
				if(b2 == b) {
					return false;
				}
				b = b2;
				s++;
			}
		} catch (BufferUnderflowException e) {
			
		}
		return s == 3;
	}
	
	public static boolean charsetSPSSTest(Charset charset) {
		String origin = "$FL2";
		byte[] data = origin.getBytes(Charsets.US_ASCII);
		String testString = new String(data, charset); 
		
		if(!origin.equals(testString)) {
			return false;
		}
		
		try {
			charset.encode(testString);
			
		} catch (UnsupportedOperationException e) {
			return false;
		}
		
		
		return true;
	}
}
