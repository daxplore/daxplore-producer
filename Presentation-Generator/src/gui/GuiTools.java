package gui;

/**
 * Various help methods for the GUI are located in this class.
 * @author hkfs89
 *
 */
public class GuiTools {
	
	static boolean checkFlag = false; // activate the version check?

	/**
	 * Checks if user is running Java 7 or above. Returns true if so.
	 * @return
	 */
	public static boolean javaVersionCheck() {
		
		if (checkFlag == false)
			return true;
		
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
