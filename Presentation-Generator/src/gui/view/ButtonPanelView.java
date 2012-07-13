package gui.view;

import gui.GUIMain;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class ButtonPanelView {
	private ButtonGroup buttonGroup;

	public ButtonPanelView() {
		this.buttonGroup = new ButtonGroup();
	}

	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	public void setButtonGroup(ButtonGroup buttonGroup) {
		this.buttonGroup = buttonGroup;
	}

	/**
	 * Handles the display and functionality of the left panel buttons.
	 * @wbp.parser.entryPoint
	 * @param mainPanel
	 */
	public JPanel radioButtonCreator(final GUIMain guiMain) {
		JPanel buttonPanel = new JPanel();	
		buttonPanel.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.LIGHT_GRAY));
		buttonPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton OpenButton = new JRadioButton("");
		OpenButton.setToolTipText("Manage file(s)");
		OpenButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("openPanel");
			}
		});
		
		OpenButton.setRolloverEnabled(false);
		OpenButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/8_selected.png")));
		OpenButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(OpenButton);
		OpenButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/8.png")));
		buttonPanel.add(OpenButton);
		
		JRadioButton ImportButton = new JRadioButton("");
		ImportButton.setToolTipText("Import SPSS file(s)");
		ImportButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("importPanel");
			}
		});
		
		ImportButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/4_selected.png")));
		ImportButton.setRolloverEnabled(false);
		ImportButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(ImportButton);
		ImportButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/4.png")));
		buttonPanel.add(ImportButton);
	
		JRadioButton QuestionsButton = new JRadioButton("");
		QuestionsButton.setToolTipText("Question edit");
		QuestionsButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("questionsPanel");
			}
		});
		
		QuestionsButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/6_selected.png")));
		QuestionsButton.setRolloverEnabled(false);
		QuestionsButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(QuestionsButton);
		QuestionsButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/6.png")));
		buttonPanel.add(QuestionsButton);
	
		JRadioButton EditButton = new JRadioButton("");
		EditButton.setToolTipText("Edit SPSS data");
		EditButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("editPanel");
			}
		});
		
		EditButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/21_selected.png")));
		EditButton.setRolloverEnabled(false);
		EditButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(EditButton);
		EditButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/21.png")));
		buttonPanel.add(EditButton);
		
		JRadioButton SortButton = new JRadioButton("");
		SortButton.setToolTipText("Sort data");
		SortButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("sortPanel");
			}
		});
		
		
		SortButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/24_selected.png")));
		SortButton.setRolloverEnabled(false);
		SortButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(SortButton);
		SortButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/24.png")));
		buttonPanel.add(SortButton);
		
		JRadioButton PackageButton = new JRadioButton("");
		PackageButton.setToolTipText("Package files");
		PackageButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				guiMain.switchTo("packagePanel");
			}
		});
		
		PackageButton.setSelectedIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/28_selected.png")));
		PackageButton.setRolloverEnabled(false);
		PackageButton.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getButtonGroup().add(PackageButton);
		PackageButton.setIcon(new ImageIcon(GUIMain.class.getResource("/gui/resources/28.png")));
		buttonPanel.add(PackageButton);
		
		return buttonPanel;
	}
}