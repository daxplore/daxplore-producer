package gui.controller;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import uiclient.OpenModule;
import uiclient.SPSSTableTest;

public class OpenController implements ActionListener {

	private JButton openbutton;
	JFileChooser filechooser;
	
	/**
	 * Method to open the file dialogue window.
	 * TODO: Rewrite functional parts to fit the new GUI.
	 */
	public OpenModule(){
		add(new Label("OpenFrame"));
		openbutton = new JButton("Open file...");
		openbutton.addActionListener(this);
		filechooser = new JFileChooser();
		filechooser.addChoosableFileFilter(new SavFilter());
		add(openbutton);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == openbutton){
            int returnVal = filechooser.showOpenDialog(OpenModule.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filechooser.getSelectedFile();
                //This is where a real application would open the file.
                System.out.println("Opening: " + file.getName() + ".");
                SPSSTableTest spssout = new SPSSTableTest(file);
                spssout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            } else {
            	System.out.println("Open command cancelled by user.");
            }
		}
		
	}
	
	private class SavFilter extends FileFilter{
		private final String[] okFileExtensions = new String[] {"sav"};

		public boolean accept(File file) {
			if(file.isDirectory()){
				return true;
			}
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return "SPSS files";
		}
		
	}
}
