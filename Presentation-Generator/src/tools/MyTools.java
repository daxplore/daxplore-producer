package tools;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

public class MyTools {

	private static boolean debugMode = true;

	public static <T> String join(Iterable<T> iter, String seperator) {
		Iterator<T> i = iter.iterator();
		StringBuilder sb = new StringBuilder();
		if(i.hasNext()){
			for (;;) {
				sb.append(i.next().toString());
				if (!i.hasNext())
					break;
				sb.append(seperator);
			}
		}
		return sb.toString();
	}

	public static <T> String join(T[] array, String seperator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (array.length - 1); i++) {
			sb.append(array[i].toString());
			sb.append(seperator);
		}
		sb.append(array[array.length - 1]);
		return sb.toString();
	}

	public static String join(int[] array, String seperator) {
		// return join(Arrays.asList(array), seperator);
		Integer[] wrap = new Integer[array.length];
		for (int i = 0; i < wrap.length; i++) {
			wrap[i] = array[i];
		}
		return join(wrap, seperator);
	}

	public static String join(double[] array, String seperator) {
		return join(Arrays.asList(array), seperator);
	}

	public static String join(float[] array, String seperator) {
		return join(Arrays.asList(array), seperator);
	}

	public static String join(char[] array, String seperator) {
		return join(Arrays.asList(array), seperator);
	}

	public static String join(long[] array, String seperator) {
		return join(Arrays.asList(array), seperator);
	}

	public static String join(boolean[] array, String seperator) {
		return join(Arrays.asList(array), seperator);
	}

	/*
	 * Experiment with map functions.
	 * 
	public static <S, T> S[] map(T array[], IndexedMapFunction<S, T> mapFunction) {
        int size = array.length;
        S t = new S();
        S[] output = Array.newInstance(, size);
        for (int i = 0; i < size; i++) {
            mapFunction.map(i, array[i]);
        }
		return null;
	}
	
	public static <S, T> List<S> map(List<T> list, IndexedMapFunction<S, T> mapFunction) {
        int size = list.size();
        List<S> output = new ArrayList<S>();
        for (int i = 0; i < size; i++) {
            output.add(mapFunction.map(i, list.get(i)));
        }
		return output;
	}
	
	public interface IndexedMapFunction<S, T> {
	    public S map(int i, T obj);
	}
	*/
	
	public static boolean debugMode() {
		return debugMode;
	}

	public static void print(String text) {
		if (debugMode)
			System.out.print(text);
	}

	public static void print(Object anObject) {
		MyTools.print(anObject.toString());
	}

	public static void print(byte aByte) {
		MyTools.print("" + aByte);
	}

	public static void print(short aShort) {
		MyTools.print("" + aShort);
	}

	public static void print(int anInt) {
		MyTools.print("" + anInt);
	}

	public static void print(long aLong) {
		MyTools.print("" + aLong);
	}

	public static void print(float aFloat) {
		MyTools.print("" + aFloat);
	}

	public static void print(double aDouble) {
		MyTools.print("" + aDouble);
	}

	public static void print(boolean aBoolean) {
		MyTools.print("" + aBoolean);
	}

	public static void print(char aChar) {
		MyTools.print("" + aChar);
	}

	public static void println(String text) {
		if (debugMode)
			System.out.println(text);
	}

	public static void println(Object anObject) {
		MyTools.println(anObject.toString());
	}

	public static void println(byte aByte) {
		MyTools.println(Byte.toString(aByte));
	}

	public static void println(short aShort) {
		MyTools.println("" + aShort);
	}

	public static void println(int anInt) {
		MyTools.println("" + anInt);
	}

	public static void println(long aLong) {
		MyTools.println("" + aLong);
	}

	public static void println(float aFloat) {
		MyTools.println("" + aFloat);
	}

	public static void println(double aDouble) {
		MyTools.println("" + aDouble);
	}

	public static void println(boolean aBoolean) {
		MyTools.println("" + aBoolean);
	}

	public static void println(char aChar) {
		MyTools.println("" + aChar);
	}

	/**
	 * Code taken from http://www.rgagnon.com/javadetails/java-0013.html
	 */
	public static String justifyLeft(String text, int width) {
		StringBuffer buf = new StringBuffer(text);
		int lastspace = -1;
		int linestart = 0;
		int i = 0;

		while (i < buf.length()) {
			if (buf.charAt(i) == ' ')
				lastspace = i;
			if (buf.charAt(i) == '\n') {
				lastspace = -1;
				linestart = i + 1;
			}
			if (i > linestart + width - 1) {
				if (lastspace != -1) {
					buf.setCharAt(lastspace, '\n');
					linestart = lastspace + 1;
					lastspace = -1;
				} else {
					buf.insert(i, '\n');
					linestart = i + 1;
				}
			}
			i++;
		}
		return buf.toString();
	}

	public static String justifyHTML(String text, int width) {
		return justifyLeft(text, width).replace("\n", "<br>");
	}

	public static String splitInTwoHTML(String text) {
		int splitPosition = text.length() / 2;
		splitPosition = text.indexOf(' ', splitPosition);
		if (splitPosition > 0) {
			return justifyHTML(text, splitPosition);
		} else {
			return text;
		}
	}

	public static boolean equalsAnyIgnoreCase(String in, String[] compareToList){
		boolean equals = false;
		for(int i = 0; i < compareToList.length; i++){
			equals = equals || in.equalsIgnoreCase(compareToList[i]);
		}
		return equals;
	}
	
	public static boolean equalsAny(String in, String[] compareToList){
		boolean equals = false;
		for(int i = 0; i < compareToList.length; i++){
			equals = equals || in.equals(compareToList[i]);
		}
		return equals;
	}
	
	public static void printSQLExeption(SQLException e) {
		System.err.println("------ SQLException --------");
		System.err.println("Message: " + e.getMessage());
		System.err.println("State: " + e.getSQLState());
		System.err.println("Code: " + e.getErrorCode());
		System.err.println(e.getStackTrace());
		System.err.println("=================== Exception chain =====================");
		Iterator<Throwable> ex = e.iterator();
		while(ex.hasNext()) {
			Throwable t = ex.next();
			System.err.println(t.getMessage());
			System.err.println(t.getStackTrace());
		}
		System.err.println("------ End of SQLException -------");
	}
	
	/**
	 * Creates an array with the numbers from to to. Inclusive.
	 * @param from
	 * @param to
	 * @return an int[]
	 */
	public static int[] range(int from, int to) {
		int size = to - from;
		int step = 1;
		if(size < 0) {
			size = -size;
			step = -1;
		}
		int[] array = new int[size+1];
		for(int i = 0; i < size; i++) {
			array[i] = from;
			from += step;
		}
		return array;
	}
}
