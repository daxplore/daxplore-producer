package org.daxplore.producer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HTMLLabelTest extends JFrame {

	public static void main(String[] args) throws Exception {
		javax.swing.text.html.parser.ParserDelegator parserDelegator = new javax.swing.text.html.parser.ParserDelegator();
		
		SwingUtilities.invokeAndWait(new Runnable() {
	        @Override
	        public void run() {
	            JFrame frame = new HTMLLabelTest();
	            frame.pack();
	            frame.setVisible(true);
	        }
	    });
	}
	
	public HTMLLabelTest() {
	
	    addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	            System.exit(0);
	        }
	    });
		
		Font font = new Font("Arial", Font.PLAIN, 12);
	    JLabel label = new JLabel();
	    label.setText("<html><body><b>Some HTML Formatted</b> text<br>another line of <i>text</i><br><h2>testing more</h2><body></html>");
	    label.setFont(font);
	    label.setOpaque(false);
	    label.setHorizontalAlignment(JLabel.CENTER);
	    label.setHorizontalTextPosition(JLabel.CENTER);
	    label.setPreferredSize(new Dimension(30,20));
	
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	    panel.add(label);
	
	    panel.setOpaque(true);
	    panel.setBackground(Color.WHITE);
	
	    add(panel);
	    setPreferredSize(new Dimension(300,200));
	}
}