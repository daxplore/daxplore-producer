package tools;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.SortedMap;
 
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
			System.out.print("(" + s + " bytes)\n");
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
				} else {
					b = b2;
				}
				s++;
			}
		} catch (BufferUnderflowException e) {
			
		}
		return s == 3;
	}
}