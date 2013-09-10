package org.daxplore.producer.uploader;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

@SuppressWarnings("serial")
public class RemoteUploader extends JFrame {

	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	private JPanel panel;
	private JTextArea area;

	public RemoteUploader() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JToolBar toolbar = new JToolBar();
		JButton openb = new JButton("open");

		openb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser fileopen = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("c files", "c");
				fileopen.addChoosableFileFilter(filter);

				int ret = fileopen.showDialog(panel, "Open file");

				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fileopen.getSelectedFile();
					try {
						dostuff(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		toolbar.add(openb);

		area = new JTextArea();
		area.setText("Select a file\n");
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(area);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(pane);
		add(panel);

		add(toolbar, BorderLayout.NORTH);

		setTitle("Remote Uploader");
		setSize(400, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				RemoteUploader ex = new RemoteUploader();
				ex.setVisible(true);
			}
		});
	}

	public void dostuff(File file) throws IOException {
		/*
		 * //Set username and password from command line, for uploading non-locally String username = System.console().readLine("username: "); String password = new String(System.console().readPassword("password: "));
		 */
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		area.append(file.getAbsolutePath() + " opened\n");
		int lines = 0;
		String line;
		while ((line = raf.readLine()) != null) {
			lines++;
		}
		lines--;
		System.out.println("Uploading " + lines + " entries");

		raf.seek(0);

		RemoteApiOptions options = new RemoteApiOptions().server("localhost", 8888).credentials("foo@bar.com", "");
		RemoteApiInstaller installer = new RemoteApiInstaller();
		installer.install(options);
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			String[] tokens;
			int i = 0;
			line = raf.readLine();
			if (line.equals("key,json")) {
				while ((line = raf.readLine()) != null) {
					tokens = line.split(",", 2);
					if (tokens.length > 1) {
						new StatStore(tokens[0], tokens[1], pm);
						if ((++i % 100) == 0) {
							area.append("" + i + "/" + lines + " uploaded\n");
						}
					}
				}
				area.append("Uploaded " + i + " entries\n");

			} else {
				area.append("error in file");
			}

		} finally {
			raf.close();
			installer.uninstall();
		}
	}
}