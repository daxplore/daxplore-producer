package gui;

public class GUITools {

	public static boolean javaVersionCheck() {
		String javaVersion = System.getProperty("java.version");
		System.out.println("Java version " + javaVersion + " was found.");
		String[] javaVersionSplit = javaVersion.split("\\.");
		int indexZero = Integer.parseInt(javaVersionSplit[0]);
		int indexOne = Integer.parseInt(javaVersionSplit[1]);

		if (!(indexZero >= 1) || (indexOne < 7))
		{
			return false;
		}
		
		return true;
	}
}
