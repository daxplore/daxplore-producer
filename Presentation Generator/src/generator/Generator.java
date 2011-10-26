package generator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Label;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Generator implements Observer{

	private JFrame frame;
	List<Module> modules = new LinkedList<Module>();
	private JPanel east = new JPanel(new CardLayout());

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Generator window = new Generator();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Generator() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		modules.add(new Module(new OpenModule(), new ImageIcon("img/chrome.jpg"), new ImageIcon("img/firefox.jpg"), "Open"));
		modules.add(new Module(new TabsModule(), new ImageIcon("img/ie9.png"), new ImageIcon("img/ie8.jpg"), "Tabs"));
		modules.add(new Module(new VarChoiceModule(), new ImageIcon("img/safari.jpg"), new ImageIcon("img/opera.jpg"), "VarChoice"));
		ButtonArray bArray = new ButtonArray(modules);
		bArray.addObserver(this);
		
		frame.add(bArray, BorderLayout.WEST);
		east.setSize(250, 300);
		for(Module m: modules){
			east.add(m.panel, m.name);
		}
		frame.add(east, BorderLayout.EAST);
			
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		Module m = (Module) arg1;
		System.out.println("Adding: " + m.name);
		CardLayout cl = (CardLayout)east.getLayout();
		cl.show(east, m.name);
	}

}
