package gui.widget;

import daxplorelib.metadata.MetaScale;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ScaleEditorWidget extends OurListWidget {

	private MetaScale.Option option;
	private TextWidget textref;
	private JTextField numberlinetext;
	private JTextField valuetext;
	
	public ScaleEditorWidget() {
		setLayout(new GridLayout(3, 1, 0, 0));
		
		textref = new TextWidget();
		add(textref);
		
		JPanel numberlinePanel = new JPanel();
		numberlinePanel.add(new JLabel("Numberline: "));
		numberlinetext = new JTextField();
		numberlinePanel.add(numberlinetext);
		add(numberlinePanel);
		
		JPanel valuePanel = new JPanel();
		valuePanel.add(new JLabel("Value: "));
		valuetext = new JTextField();
		valuePanel.add(valuetext);
		add(valuePanel);
	}
	
	public void setOption(MetaScale.Option option) {
		this.option = option;
		textref.setTextReference(option.getTextRef());
		textref.showEdit(true);
		numberlinetext.setText(option.getTransformation().toString());
		valuetext.setText(Double.toString(option.getValue()));
	}
	
	public MetaScale.Option getOption() {
		return option;
	}
	
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = textref.getPreferredSize();
        dim.height *= 3;
        return dim;
    }
}
